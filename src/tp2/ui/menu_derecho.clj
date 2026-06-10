(ns tp2.ui.menu-derecho
  (:require [tp2.eventos.acciones :as evt]
            [tp2.eventos.estado :as est])
  (:import (java.awt Color Dimension)
           (java.awt.event ActionListener)
           (javax.swing BorderFactory Box BoxLayout JButton JPanel SwingUtilities)))
;; Configuracion de las acciones disponibles en el panel lateral derecho.
(def config-top
  [{:texto "Brillo" :accion :brillo}
   {:texto "Difuminado" :accion :difuminar}
   {:texto "Invertir" :accion :invertir}])

(def config-bottom
  [{:texto "Agregar" :accion :agregar}
   {:texto "Aplicar" :accion :aplicar}
   {:texto "Deshacer" :accion :deshacer}
   {:texto "Reset" :accion :resetear}])

(defn crear []
  (let [panel (JPanel.)
        borde-linea (BorderFactory/createMatteBorder 0 1 0 0 Color/GRAY)
        borde-vacio (BorderFactory/createEmptyBorder 20 20 20 20)
        color-fondo (Color. 230 230 230)
        color-boton (Color. 180 180 180)
        color-activo (Color. 100 200 100)
        color-deshabilitado (Color. 130 130 130)
        _ (Color. 255 100 100)
        ;; Registro local para guardar instancias de botones y poder modificarlos
        botones-estado (atom {})
        crear-btn (fn [{:keys [texto accion]}]
                    (let [btn (JButton. texto)]
                      (.setMaximumSize btn (Dimension. Integer/MAX_VALUE (.height (.getPreferredSize btn))))
                      (.setBackground btn color-deshabilitado)
                      (.setEnabled btn false)
                      (.addActionListener btn
                                          (reify ActionListener
                                            (actionPerformed [_ _]
                                              (evt/manejar-accion accion))))
                      (swap! botones-estado assoc accion btn)
                      btn))]

    (.setLayout panel (BoxLayout. panel BoxLayout/Y_AXIS))
    (.setPreferredSize panel (Dimension. 200 900))
    (.setBackground panel color-fondo)
    (.setBorder panel (BorderFactory/createCompoundBorder borde-linea borde-vacio))

    (doseq [cfg config-top]
      (.add panel (crear-btn cfg)))

    (.add panel (Box/createVerticalGlue))
    (doseq [cfg config-bottom]
      (.add panel (crear-btn cfg)))

    ;; Sincroniza la apariencia y estado de los botones con la seleccion de filtros
    (add-watch est/estado :actualizador-botones
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (let [seleccionado (:filtro-seleccionado nuevo-estado)
                           pipeline (:pipeline nuevo-estado)
                           aplicados (:filtros-aplicados nuevo-estado)
                           hay-imagen? (some? (:imagen nuevo-estado))]
                       (doseq [[accion btn] @botones-estado]

                           (let [habilitado (cond
                                              (= accion :deshacer) (boolean (and hay-imagen? seleccionado (or (some #{seleccionado} pipeline) (some #{seleccionado} aplicados))))
                                              (= accion :agregar) (boolean (and hay-imagen? seleccionado))
                                              (= accion :aplicar) (boolean (and hay-imagen? (seq pipeline)))
                                              :else hay-imagen?)]
                             (.setEnabled btn habilitado)
                             (if habilitado
                               (if (or (= seleccionado accion)
                                       (some #{accion} pipeline)
                                       (some #{accion} aplicados))
                                 (.setBackground btn color-activo)
                                 (.setBackground btn color-boton))
                               (.setBackground btn color-deshabilitado)))))))))

    panel))