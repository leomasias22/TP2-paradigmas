(ns tp2.app
  (:gen-class)
  (:require [tp2.ui.menu-izquierdo :as izq]
            [tp2.ui.menu-derecho :as der]
            [tp2.menu-eventos :as evt])
  (:import [javax.swing JFrame JPanel SwingUtilities JLabel ImageIcon JScrollPane]
           [java.awt BorderLayout Color Cursor]))

(defn crear-panel-central []
  (let [panel (JPanel. (BorderLayout.))
        label-imagen (JLabel. "")
        scroll-pane (JScrollPane. label-imagen)
        color-fondo (Color. 250 250 250)]

    (.setBackground panel color-fondo)
    (.setHorizontalAlignment label-imagen JLabel/CENTER)
    (.setVerticalAlignment label-imagen JLabel/CENTER)

    (.add panel scroll-pane BorderLayout/CENTER)

    ;; Escucha cambios en el estado para actualizar la vista en el hilo de Swing
    (add-watch evt/estado :actualizador-vista
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

(defn crear-interfaz []
  (let [frame (JFrame. "Interfaz")
        panel-izquierdo (izq/crear)
        panel-derecho (der/crear)
        panel-central (crear-panel-central)]

    (.setLayout frame (BorderLayout.))
    (.add frame panel-izquierdo BorderLayout/WEST)
    (.add frame panel-central BorderLayout/CENTER)
    (.add frame panel-derecho BorderLayout/EAST)

    (.setSize frame 1600 900)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setLocationRelativeTo frame nil)

    ;; Controla el cursor de carga basandose en el estado de procesamiento
    (add-watch evt/estado :actualizador-cursor
               (fn [_ _ _ nuevo-estado]
                 (SwingUtilities/invokeLater
                   (fn []
                     (if (:procesando? nuevo-estado)
                       (.setCursor frame (Cursor/getPredefinedCursor Cursor/WAIT_CURSOR))
                       (.setCursor frame (Cursor/getDefaultCursor)))))))

    (.setVisible frame true)))

(defn -main [& args];Para no volvernos locos deployando dejo esto aca, despues borrar.
  (SwingUtilities/invokeLater
    (fn []
      (crear-interfaz))))