(ns tp2.ui.menu-derecho
  (:require [tp2.menu-eventos :as evt])
  (:import (java.awt.event ActionListener)
           [javax.swing JPanel JButton BoxLayout BorderFactory Box SwingUtilities]
           [java.awt Dimension Color]))

(def config-top
  [{:texto "Desaturar" :accion :desaturar}
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
        color-rojo (Color. 255 100 100)
        ;; Registro para guardar instancias de botones
        botones-estado (atom {})
        crear-btn (fn [{:keys [texto accion]}]
                    (let [btn (JButton. texto)]
                      (.setMaximumSize btn (Dimension. Integer/MAX_VALUE (.height (.getPreferredSize btn))))
                      (.setBackground btn color-deshabilitado)
                      (.setEnabled btn false)
                      (.addActionListener btn
                                          (reify java.awt.event.ActionListener
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

    ;; Empuja los botones inferiores al fondo del panel
    (.add panel (Box/createVerticalGlue))

    (doseq [cfg config-bottom]
      (.add panel (crear-btn cfg)))

    ;; Sincroniza los botones de UI con el estado logico de los filtros
    (add-watch evt/estado :actualizador-botones
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (let [seleccionado (:filtro-seleccionado nuevo-estado)
                           pipeline (:pipeline nuevo-estado)
                           aplicados (:filtros-aplicados nuevo-estado)
                           hay-imagen? (some? (:imagen nuevo-estado))]
                       (doseq [[accion btn] @botones-estado]
                         (if (= accion :desaturar);despues modificar cuando se implemente desaturar.
                           (do
                             (.setEnabled btn false)
                             (.setBackground btn color-rojo))
                           (let [habilitado (if (= accion :deshacer)
                                              (boolean (and hay-imagen? seleccionado (get aplicados seleccionado)))
                                              hay-imagen?)]
                             (.setEnabled btn habilitado)
                             (if habilitado
                               (if (or (= seleccionado accion)
                                       (some #{accion} pipeline)
                                       (get aplicados accion))
                                 (.setBackground btn color-activo)
                                 (.setBackground btn color-boton))
                               (.setBackground btn color-deshabilitado))))))))))

    panel))