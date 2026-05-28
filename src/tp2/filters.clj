(ns tp2.filters
  (:require [tp2.imagenes :as img]))

; "Funciones puras de procesamiento de imagen."

(defn brillo-pixel [pixel factor-brillo]
  ; Dado un pixel [un map de 4 campos], devuelve un pixel con brillo aumentado.
  {:a (get pixel :a)
   :r (img/clamp (* (get pixel :r) factor-brillo))
   :g (img/clamp (* (get pixel :g) factor-brillo))
   :b (img/clamp (* (get pixel :b) factor-brillo))
   })

(defn brillo [matriz-pixeles]
  ; Aumenta el brillo de la imagen, multiplicando una constante pixel por pixel.
  (let [factor-brillo 1.1]
    (mapv (fn [fila-pixeles]
            (mapv (fn [pixel] (brillo-pixel pixel factor-brillo)) fila-pixeles))
          matriz-pixeles)
    ))

(defn blur [matriz-pixeles]
  ; Difumina la imagen, reduciendo detalles finos y el ruido local.
  ; Este filtro usa un kernel de suavizado de caja 3x3.


  )

(defn invertir-pixel [pixel]
  ; Dado un pixel [un map de 4 campos], devuelve un pixel con campos RGBA invertidos.
  {:a (get pixel :a)
   :r (- 255 (get pixel :r))
   :g (- 255 (get pixel :g))
   :b (- 255 (get pixel :b))
   })

(defn invertir [matriz-pixeles]
  ; Invierte los colores de la imagen, modificando el RGB pixel por pixel
  (mapv (fn [fila-pixeles]
          (mapv (fn [pixel] (invertir-pixel pixel)) fila-pixeles))
        matriz-pixeles)
  )
