(ns tp2.ui.panel-filtros
  (:require [tp2.eventos.acciones :as evt])
  (:import (java.awt Color Dimension)
           (java.awt.event ActionListener)
           (javax.swing BorderFactory Box BoxLayout JButton JLabel JPanel SwingUtilities)))


; ngl es casi patcheado pero anda OK
(defn crear [estado]
  (let [panel (JPanel.)
        borde-linea (BorderFactory/createMatteBorder 1 1 1 1 Color/GRAY)
        borde-vacio (BorderFactory/createEmptyBorder 0 0 0 0)
        color-fondo (Color. 230 230 230)
        _ (Color. 255 100 100)
        crear-item-filtro (fn [idx texto]
                    (let [item (JPanel.)
                          label (JLabel. texto)
                          btn (JButton. "x")]
                      (.setLayout item (BoxLayout. item BoxLayout/X_AXIS))
                      (.setMinimumSize label (Dimension. 120 40))
                      (.setMinimumSize btn (Dimension. 40 40))
                      (.setMinimumSize item (Dimension. 160 40))
                      (.add item label)
                      (.add item btn)
                      (.addActionListener btn
                                          (reify ActionListener
                                            (actionPerformed [_ _]
                                              (evt/manejar-accion :quitar-filtro idx estado))))
                      item))]

    (.setLayout panel (BoxLayout. panel BoxLayout/Y_AXIS))
    (.setMinimumSize panel (Dimension. 160 160))
    (.setPreferredSize panel (Dimension. 160 600))
    (.setMaximumSize panel (Dimension. 160 Short/MAX_VALUE))
    (.setBackground panel color-fondo)
    (.setBorder panel (BorderFactory/createCompoundBorder borde-linea borde-vacio))

    ;; Sincroniza la apariencia y estado de los botones con la seleccion de filtros
    (add-watch estado :actualizador-items
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (.removeAll panel)
                     (doseq [[idx filtro] (map-indexed vector (:pipeline nuevo-estado))]
                       (let [texto (name filtro)
                             item (crear-item-filtro idx texto)]
                         (.add panel item)))

                     (.revalidate panel)
                     (.repaint panel)
                     ))
                 ))
    panel))