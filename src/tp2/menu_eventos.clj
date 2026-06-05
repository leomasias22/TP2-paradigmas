(ns tp2.menu-eventos
  (:require [tp2.imagenes :as img]
            [tp2.paralelismo :as par]
            [tp2.filters :as flt])
  (:import [javax.imageio ImageIO]
           [java.io File]
           [java.awt FileDialog Frame]
           [java.awt.image BufferedImage]))

;; Estado global del sistema gestionado de forma segura
(def estado (atom {:ruta nil
                   :imagen nil
                   :imagen-original nil
                   :filtro-seleccionado nil
                   :pipeline []
                   :procesando? false
                   :filtros-aplicados {}}))

;; Enrutamiento de eventos basado en la accion
(defmulti manejar-accion (fn [accion & args] accion))

(defmethod manejar-accion :default [accion & args]
  (println "Accion no implementada:" accion))

(defmethod manejar-accion :cargar [_ ruta]
  (try
    (let [img (ImageIO/read (File. ruta))]
      (reset! estado {:ruta ruta
                      :imagen img
                      :imagen-original img
                      :filtro-seleccionado nil
                      :pipeline []
                      :procesando? false
                      :filtros-aplicados {}}))
    (catch Exception e
      (println "Error:" (.getMessage e)))))

(defmethod manejar-accion :abrir [_]
  (let [dialogo (FileDialog. (Frame.) "Seleccionar Imagen" FileDialog/LOAD)]
    (.setFile dialogo "*.png;*.jpg;*.jpeg")
    (.setVisible dialogo true)
    (let [directorio (.getDirectory dialogo)
          archivo (.getFile dialogo)]
      (when (and directorio archivo)
        (manejar-accion :cargar (str directorio archivo))))))

(defmethod manejar-accion :guardar [_]
  (let [{:keys [ruta imagen]} @estado]
    (when (and ruta imagen)
      (try
        (let [ext (if (.endsWith (.toLowerCase ruta) "png") "png" "jpg")
              archivo (File. ruta)]
          (ImageIO/write imagen ext archivo)
          (println "Guardado en:" ruta))
        (catch Exception e
          (println "Error:" (.getMessage e)))))))

(defmethod manejar-accion :salir [_]
  (System/exit 0))

(defn seleccionar-filtro [filtro]
  (when (:imagen @estado)
    (swap! estado assoc :filtro-seleccionado filtro)))

(defmethod manejar-accion :desaturar [_] (seleccionar-filtro :desaturar));;Cuando este implementado desaturar agregar a aplicar-filtros-activos
(defmethod manejar-accion :difuminar [_] (seleccionar-filtro :difuminar))
(defmethod manejar-accion :invertir [_] (seleccionar-filtro :invertir))

(defmethod manejar-accion :agregar [_]
  (let [{:keys [filtro-seleccionado]} @estado]
    (when filtro-seleccionado
      (swap! estado update :pipeline conj filtro-seleccionado))))

(defmethod manejar-accion :resetear [_]
  (let [{:keys [imagen-original]} @estado]
    (when imagen-original
      (swap! estado assoc
             :imagen imagen-original
             :pipeline []
             :filtro-seleccionado nil
             :filtros-aplicados {}))))

;; Recalcula la cadena entera de efectos activos desde la imagen original
(defn aplicar-filtros-activos [imagen-original mapa-activos]
  (let [filtros-activos (keys mapa-activos)
        mapa-filtros {:difuminar flt/difuminado
                      :invertir flt/invertir}]
    (if (empty? filtros-activos)
      imagen-original
      (let [matriz-pixeles (img/obtener-pixeles imagen-original)
            filtros-a-aplicar (keep mapa-filtros filtros-activos)
            resultado-matriz @(par/aplicar-pipeline filtros-a-aplicar matriz-pixeles)
            w (.getWidth imagen-original)
            h (.getHeight imagen-original)
            nueva-img (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
            pixeles-planos (apply concat resultado-matriz)
            arreglo-enteros (int-array (map img/empaquetar-pixel pixeles-planos))]
        (.setRGB nueva-img 0 0 w h arreglo-enteros 0 w)
        nueva-img))))

;; Elimina la clave del filtro a deshacer
(defmethod manejar-accion :deshacer [_]
  (let [{:keys [imagen-original filtro-seleccionado filtros-aplicados]} @estado]
    (when (and filtro-seleccionado (get filtros-aplicados filtro-seleccionado))
      (swap! estado assoc :procesando? true)
      (future
        (let [nuevo-mapa (dissoc filtros-aplicados filtro-seleccionado)
              nueva-img (aplicar-filtros-activos imagen-original nuevo-mapa)]
          (swap! estado assoc
                 :filtros-aplicados nuevo-mapa
                 :imagen nueva-img
                 :filtro-seleccionado nil
                 :procesando? false))))))

;; Consolida los filtros en el mapa de filtros aplicados
(defmethod manejar-accion :aplicar [_]
  (let [{:keys [imagen-original pipeline filtros-aplicados]} @estado]
    (when (and imagen-original (seq pipeline))
      (swap! estado assoc :procesando? true)
      (future
        (let [nuevo-mapa (reduce #(assoc %1 %2 true) filtros-aplicados pipeline)
              nueva-img (aplicar-filtros-activos imagen-original nuevo-mapa)]
          (swap! estado assoc
                 :filtros-aplicados nuevo-mapa
                 :imagen nueva-img
                 :pipeline []
                 :filtro-seleccionado nil
                 :procesando? false))))))