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

(defn avg-kernel [subvector-pixeles]
  ; Dada una submatriz de pixeles, calcula el pixel promedio.
  (let [canales [:a :r :g :b]
        pixeles-validos (filter some? subvector-pixeles)
        n (max 1 (count pixeles-validos))]
    ; Crear un mapa de formato adecuado [canales : sumas/n]
    (zipmap canales
            (for [canal canales]
              (quot (reduce + (map canal pixeles-validos)) n)))
    ))

(defn difuminado-pixel [matriz-pixeles x y]
  ; Dada una matriz de pixeles y coordenadas de un pixel, calcula un subvector de pixeles,
  ; luego delega al kernel la tarea de promedio de pixel, y devuelve su resultado.
  (let [subvector-pixeles (for [ady-x [-1 0 1] ady-y [-1 0 1]]
                            (img/obtener-pixel matriz-pixeles (+ x ady-x) (+ y ady-y))
                            )]
    (avg-kernel subvector-pixeles)
  ))

(defn difuminado [matriz-pixeles]
  ; Difumina la imagen, reduciendo detalles finos y el ruido local.
  ; Este filtro usa un kernel de suavizado de caja 3x3.
  (let [alto (count matriz-pixeles)
        ancho (count (first matriz-pixeles))]
    (vec (for [y (range alto)]
           (vec (for [x (range ancho)]
                  (difuminado-pixel matriz-pixeles x y)))
           ))
    ))

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
