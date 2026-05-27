(ns tp2.imagenes
  (:import [javax.imageio ImageIO]
           [java.io File]))

"Funciones para abrir / administrar imagenes"

(defn clamp
  "Dado un entero, devuelve uno dentro del rango [0, 255]"
  [num]
  (cond
    (< num 0) 0
    (> num 255) 255
    :else num))

(defn cargar_imagen [filepath]
  (ImageIO/read (File. filepath)))

(defn guardar_imagen [imagen extension filepath]
  (ImageIO/write imagen extension (File. filepath)))

(defn- desempaquetar_pixel [pixel]
  "Dado un entero, extrae los canales RGB en un Map."
  {:r (bit-and (bit-shift-right pixel 16) 0xFF)
   :g (bit-and (bit-shift-right pixel 8) 0xFF)
   :b (bit-and pixel 0xFF)})

(defn obtener_pixeles [imagen]
  "Dada una imagen, devuelve sus pixeles."
  (let [w (.getWidth imagen)
        h (.getHeight imagen)
        arreglo_pixeles (int-array (* w h))]

  "https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html#:~:text=int%29-,getRGB"
  (.getRGB imagen 0 0 w h arreglo_pixeles 0 w)
  (map desempaquetar_pixel (seq arreglo_pixeles))
  ))
