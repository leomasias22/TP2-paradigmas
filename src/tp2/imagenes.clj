(ns tp2.imagenes
  (:import [javax.imageio ImageIO]
           [java.io File]))

; "Funciones para abrir / administrar imagenes"

(defn clamp [num]
  ; Dado un entero que representa un valor de color, devuelve uno dentro del rango [0, 255]
  (cond
    (< num 0) 0
    (> num 255) 255
    :else num))

(defn obtener_pixel [matriz_pixeles x y]
  ; Dada una matriz de pixeles y unas coordenadas, devuelve un pixel.
  ; En el caso de que esté fuera de los límites, devuelve un pixel RGBA #00000000.
  (get (get matriz_pixeles x []) y 0x00000000))

(defn cargar_imagen [filepath]
  ; Dado un filepath, carga una imagen en memoria.
  (ImageIO/read (File. filepath)))

(defn guardar_imagen [imagen extension filepath]
  ; Dados una imagen, una extension de archivo (como .png, .jpg, etc) y un filepath, guarda una imagen en el disco.
  (ImageIO/write imagen extension (File. filepath)))

(defn desempaquetar_pixel [pixel]
  ; Dado un entero, extrae los canales RGBA en un Map.
  {:a (bit-and (bit-shift-right pixel 24) 0xFF)
   :r (bit-and (bit-shift-right pixel 16) 0xFF)
   :g (bit-and (bit-shift-right pixel 8) 0xFF)
   :b (bit-and pixel 0xFF)})

(defn obtener_pixeles [imagen]
  ; Dada una imagen, devuelve sus pixeles en una matriz de pixeles [que son mapas RGBA].
  (let [w (.getWidth imagen)
        h (.getHeight imagen)
        arreglo_pixeles (int-array (* w h))]

  ; https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html#:~:text=int%29-,getRGB
  (.getRGB imagen 0 0 w h arreglo_pixeles 0 w)
  ; Transformar a forma matricial:
  ; primero desempaquetar, luego particionar, finalmente vectorizar.
  ; TODO: seguramente existirá algún metodo mejor
  (mapv vec (partition-all w (mapv desempaquetar_pixel arreglo_pixeles)))
  ))
