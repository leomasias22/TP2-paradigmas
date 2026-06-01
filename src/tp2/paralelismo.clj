(ns tp2.paralelismo)

; "Espacio para jugar con un pipeline, sus filtros, y su imagen asociada;"
; "Aprovechando el paralelismo en cada capa de filtrado."

(defn calcular-rangos [total fracciones]
  ; Calcula, dado un numero y una cantidad de fracciones, una colección de rangos.
  (let [tamano (int (Math/ceil (/ total fracciones)))]
    ; (int (Math/ceil)) es para obtener el entero redondeando para arriba en vez de truncar con quot
    (map (fn [fraccion]
           (let [inicio (* fraccion tamano)
                 fin (min total (+ inicio tamano))]
             ; Esto es lo que va a devolver: varios vectores de tipo [inicio fin].
             [inicio fin]))
         (range fracciones))
    ))

(defn aplicar-filtro [matriz-pixeles filtro rangos]
  ; Dada una imagen, un filtro, y rangos para su paralelismo, devuelve una matriz de pixeles con su filtro aplicado.
  (let [submatrices (pmap (fn [[inicio fin]]
                            (filtro matriz-pixeles inicio fin))
                          rangos)]
    (vec (apply concat submatrices))
    ))

(defn aplicar-pipeline [pipeline imagen-actual]
  ; Aplica toda la secuencia de efectos del pipeline sobre la imagen original, y devuelve la imagen resultado.
  ; La imagen original debe ser una matriz de pixeles. La imagen resultado tambien tendrá formato de matriz.

  ; (actualizar el estado: se comenzaron a aplicar los filtros)
  (future
    (let [thread-count (.availableProcessors (Runtime/getRuntime))
          rangos (calcular-rangos (count imagen-actual) thread-count)
          ; DAMN!!!!!!!
          imagen-final (reduce (fn [imagen filtro]
                                 (aplicar-filtro imagen filtro rangos))
                               imagen-actual pipeline)]
      ; (actualizar el estado: se terminaron de aplicar los filtros)
      imagen-final)))
