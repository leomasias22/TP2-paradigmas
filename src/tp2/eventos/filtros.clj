(ns tp2.eventos.filtros
  (:require [tp2.eventos.acciones :refer [manejar-accion]]
            [tp2.imagenes :as img]
            [tp2.paralelismo :as par]
            [tp2.filters :as flt])
  (:import [java.awt.image BufferedImage]))

;; Registra el filtro elegido temporalmente antes de enviarlo al pipeline
(defn seleccionar-filtro [estado filtro]
  (when (:imagen @estado)
    (swap! estado assoc :filtro-seleccionado filtro)))

(defmethod manejar-accion :brillo [_ estado] (seleccionar-filtro estado :brillo))
(defmethod manejar-accion :difuminar [_ estado] (seleccionar-filtro estado :difuminar))
(defmethod manejar-accion :invertir [_ estado] (seleccionar-filtro estado :invertir))
;; Inserta el filtro seleccionado al final de la secuencia del pipeline
(defmethod manejar-accion :agregar [_ estado]
  (let [{:keys [filtro-seleccionado]} @estado]
    (when filtro-seleccionado
      (swap! estado update :pipeline conj filtro-seleccionado))))

;; Limpia toda la secuencia y devuelve la imagen a su estado original
(defmethod manejar-accion :resetear [_ estado]
  (let [{:keys [imagen-original]} @estado]
    (when imagen-original
      (swap! estado assoc
             :imagen imagen-original
             :pipeline []
             :filtros-aplicados []
             :filtro-seleccionado nil))))

;; Quita unicamente la ultima instancia del filtro seleccionado de la secuencia
(defn quitar-ultimo-filtro [pipeline filtro]
  (let [v-pipe (vec pipeline)
        idx (.lastIndexOf v-pipe filtro)]
    (if (>= idx 0)
      (let [[antes despues] (split-at idx v-pipe)]
        (vec (concat antes (rest despues))))
      pipeline)))

(defn quitar-filtro [idx pipeline]
  ; Dado un indice de filtro, devuelve un pipeline que lo haya removido.
  ; Esta funcion no revisa si se envia un indice fuera de rango; asume que es correcto.
  (vec (concat (subvec pipeline 0 idx)
               (subvec pipeline (inc idx)))
       ))

;; Recalcula la imagen pasando todos los filtros aplicados
(defn re-aplicar-filtros [imagen-original filtros-aplicados]
  (if (empty? filtros-aplicados)
    imagen-original
    (let [matriz-pixeles (img/obtener-pixeles imagen-original)
          mapa-filtros {:difuminar flt/difuminado
                        :invertir flt/invertir
                        :brillo flt/brillo};cambiar cuando se implemente desaturar.
          filtros-a-aplicar (map mapa-filtros filtros-aplicados)
          resultado-matriz @(par/aplicar-pipeline filtros-a-aplicar matriz-pixeles)
          w (.getWidth imagen-original)
          h (.getHeight imagen-original)
          nueva-img (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
          pixeles-planos (apply concat resultado-matriz)
          arreglo-enteros (int-array (map img/empaquetar-pixel pixeles-planos))]
      (.setRGB nueva-img 0 0 w h arreglo-enteros 0 w)
      nueva-img)))

;; Deshace un filtro individual quitandolo del pipeline pendiente o de los aplicados
(defmethod manejar-accion :deshacer [_ estado]
  (let [{:keys [filtro-seleccionado pipeline filtros-aplicados imagen-original]} @estado]
    (when filtro-seleccionado
      (if (some #{filtro-seleccionado} pipeline)
        (swap! estado (fn [st]
                        (-> st
                            (update :pipeline quitar-ultimo-filtro filtro-seleccionado)
                            (assoc :filtro-seleccionado nil))))
        (when (some #{filtro-seleccionado} filtros-aplicados)
          (swap! estado assoc :procesando? true)
          (future
            (let [nuevos-aplicados (quitar-ultimo-filtro filtros-aplicados filtro-seleccionado)
                  nueva-img (re-aplicar-filtros imagen-original nuevos-aplicados)]
              (swap! estado assoc
                     :filtros-aplicados nuevos-aplicados
                     :imagen nueva-img
                     :filtro-seleccionado nil
                     :procesando? false))))))))

;; Aplica la secuencia completa de filtros sobre la imagen original
(defmethod manejar-accion :aplicar [_ estado]
  (let [{:keys [imagen-original pipeline filtros-aplicados]} @estado]
    (when (and imagen-original (seq pipeline))
      (swap! estado assoc :procesando? true)
      (future
        (let [nuevos-aplicados (vec (concat filtros-aplicados pipeline))
              nueva-img (re-aplicar-filtros imagen-original nuevos-aplicados)]
          (swap! estado assoc
                 :filtros-aplicados nuevos-aplicados
                 :imagen nueva-img
                 :pipeline []
                 :filtro-seleccionado nil
                 :procesando? false))))))