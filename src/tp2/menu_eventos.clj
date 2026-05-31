(ns tp2.menu-eventos
  (:import [javax.imageio ImageIO]
           [java.io File]
           [java.awt FileDialog Frame]))

;; Este es el atomizador que guarda la imagen y su ruta
(def estado (atom {:ruta nil :imagen nil :filtro-seleccionado nil :pipeline []}))

(defmulti manejar-accion (fn [accion & args] accion))

(defmethod manejar-accion :default [accion & args]
  (println "Accion no implementada:" accion))

;; Carga la imagen y la mete en el atomizador
(defmethod manejar-accion :cargar [_ ruta]
  (try
    (let [img (ImageIO/read (File. ruta))]
      (reset! estado {:ruta ruta :imagen img :filtro-seleccionado nil :pipeline []}))
    (catch Exception e
      (println "Error:" (.getMessage e)))))

;; Abre el buscador predeterminado del sistema operativo
(defmethod manejar-accion :abrir [_]
  (let [dialogo (FileDialog. (Frame.) "Seleccionar Imagen" FileDialog/LOAD)]
    (.setFile dialogo "*.png;*.jpg;*.jpeg")
    (.setVisible dialogo true)
    (let [directorio (.getDirectory dialogo)
          archivo (.getFile dialogo)]
      (when (and directorio archivo)
        (manejar-accion :cargar (str directorio archivo))))))

;; Guarda la imagen sobre la imagen existente
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

;; Manejamos los estados de los filtros con esto (seleccion unica)
(defn seleccionar-filtro [filtro]
  (when (:imagen @estado)
    (swap! estado assoc :filtro-seleccionado filtro)))

(defmethod manejar-accion :desaturar [_] (seleccionar-filtro :desaturar))
(defmethod manejar-accion :difuminar [_] (seleccionar-filtro :difuminar))
(defmethod manejar-accion :invertir [_] (seleccionar-filtro :invertir))