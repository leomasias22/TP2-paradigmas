(ns tp2.pipeline)

; "Espacio para el pipeline, independiente de una imagen"
; Las funciones son puras, salvo se indique lo contrario.
; Requieren de un pipeline, y devuelven un pipeline nuevo

(defn agregar-filtro [pipeline filtro]
  ; Agrega un filtro al final del pipeline.
  (conj pipeline filtro))

(defn eliminar-filtro [pipeline idx]
  ; Dado un indice de filtro, devuelve un pipeline que lo haya removido.
  ; Esta funcion no revisa si se envia un indice fuera de rango.
  (vec (concat (subvec pipeline 0 idx)
               (subvec pipeline (inc idx)))
  ))

(defn reset-pipeline []
  ; Devuelve un pipeline vacio.
  [])

(defn aplicar-pipeline [pipeline imagen-original]
  ; Aplica toda la secuencia de efectos del pipeline sobre la imagen original, y devuelve la imagen resultado.
  ; La imagen original debe ser una matriz de pixeles. La imagen resultado tendrá tambien formato de matriz.
  (reduce (fn [imagen filtro] (filtro imagen))
          imagen-original pipeline
          ))

; TODO/IDEAS PARALELISMO FILTROS:
; En el namespace que administre los llamados (la UI), podría haber un bloque que se vea algo asi:
; (actualizar el estado: se comenzaron a aplicar los filtros)
; (future
;  (let [imagen-final (pipeline/aplicar-pipeline pipeline imagen-actual)]
;    (actualizar el estado: se terminaron de aplicar los filtros)
;    ))

; Asimismo, luego la UI se encargaría (CON PMAP) de dividir las tareas, y luego unir las imagenes que resulten.
