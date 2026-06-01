(ns tp2.pipeline)

; "Espacio para el pipeline, independiente de una imagen"

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
