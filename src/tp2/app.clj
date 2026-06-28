(ns tp2.app
  (:gen-class)
  (:require [tp2.ui.menu-izquierdo :as izq]
            [tp2.ui.menu-derecho :as der]
            [tp2.eventos.archivos]
            [tp2.eventos.filtros])
  (:import [javax.swing JFrame JPanel SwingUtilities JLabel ImageIcon JScrollPane]
           [java.awt BorderLayout Color Cursor]))

(defn crear-panel-central [estado]
  (let [panel (JPanel. (BorderLayout.))
        label-imagen (JLabel. "")
        scroll-pane (JScrollPane. label-imagen)
        color-fondo (Color. 250 250 250)]

    (.setBackground panel color-fondo)
    (.setHorizontalAlignment label-imagen JLabel/CENTER)
    (.setVerticalAlignment label-imagen JLabel/CENTER)

    (.add panel scroll-pane BorderLayout/CENTER)

    (add-watch estado :actualizador-vista
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (if-let [img (:imagen nuevo-estado)]
                       (let [icono (ImageIcon. img)]
                         (.setIcon label-imagen icono)
                         (.revalidate label-imagen)
                         (.repaint label-imagen))
                       (do
                         (.setIcon label-imagen nil)
                         (.revalidate label-imagen)
                         (.repaint label-imagen)))))))
    panel))

(defn crear-interfaz [estado]
  (let [frame (JFrame. "Interfaz")
        panel-izquierdo (izq/crear estado)
        panel-derecho (der/crear estado)
        panel-central (crear-panel-central estado)]

    (.setLayout frame (BorderLayout.))
    (.add frame panel-izquierdo BorderLayout/WEST)
    (.add frame panel-central BorderLayout/CENTER)
    (.add frame panel-derecho BorderLayout/EAST)

    (.setSize frame 1600 900)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setLocationRelativeTo frame nil)

    (add-watch estado :actualizador-cursor
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (if (:procesando? nuevo-estado)
                       (.setCursor frame (Cursor/getPredefinedCursor Cursor/WAIT_CURSOR))
                       (.setCursor frame (Cursor/getDefaultCursor)))))))

    (.setVisible frame true)))

(defn inicializar []
  (let [estado (atom {:ruta nil
                      :imagen nil
                      :imagen-original nil
                      :filtro-seleccionado nil
                      :pipeline []
                      :filtros-aplicados []
                      :procesando? false})]
    (SwingUtilities/invokeLater
      (fn []
        (crear-interfaz estado)))
    ))
