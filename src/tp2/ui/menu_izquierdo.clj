(ns tp2.ui.menu-izquierdo
  (:require [tp2.menu-eventos :as evt])
  (:import [javax.swing JPanel JButton BoxLayout BorderFactory]
           [java.awt Dimension Color]))

;; Configuracion base de acciones del panel izquierdo
(def config-botones
  [{:texto "Abrir" :accion :abrir}
   {:texto "Guardar" :accion :guardar}
   {:texto "Guardar como" :accion :guardar-como}
   {:texto "Salir" :accion :salir}])

(defn crear []
  (let [panel (JPanel.)
        borde-linea (BorderFactory/createMatteBorder 0 0 0 1 Color/GRAY)
        borde-vacio (BorderFactory/createEmptyBorder 20 20 20 20)
        color-fondo (Color. 230 230 230)
        color-boton (Color. 180 180 180)]

    ;; Definicion del diseno visual del contenedor
    (.setLayout panel (BoxLayout. panel BoxLayout/Y_AXIS))
    (.setPreferredSize panel (Dimension. 200 900))
    (.setBackground panel color-fondo)
    (.setBorder panel (BorderFactory/createCompoundBorder borde-linea borde-vacio))

    ;; Generacion e insercion dinamica de botones
    (doseq [{:keys [texto accion]} config-botones]
      (let [btn (JButton. texto)]
        (.setMaximumSize btn (Dimension. Integer/MAX_VALUE (.height (.getPreferredSize btn))))
        (.setBackground btn color-boton)
        (.addActionListener btn
                            (reify java.awt.event.ActionListener
                              (actionPerformed [_ _]
                                (evt/manejar-accion accion))))
        (.add panel btn)))

    panel))