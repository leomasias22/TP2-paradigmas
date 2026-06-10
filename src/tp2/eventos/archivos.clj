(ns tp2.eventos.archivos
  (:require [tp2.eventos.estado :refer [estado]]
            [tp2.eventos.acciones :refer [manejar-accion]])
  (:import [javax.imageio ImageIO]
           [java.io File]
           [java.awt FileDialog Frame]
           [java.awt.image BufferedImage]))

;; Carga la imagen desde el disco y reinicia el estado de los filtros
(defmethod manejar-accion :cargar [_ ruta]
  (try
    (let [img (ImageIO/read (File. ruta))]
      (reset! estado {:ruta ruta
                      :imagen img
                      :imagen-original img
                      :filtro-seleccionado nil
                      :pipeline []
                      :filtros-aplicados []
                      :procesando? false}))
    (catch Exception e
      (println "Error:" (.getMessage e)))))

;; Abre un cuadro de dialogo nativo para elegir el archivo
(defmethod manejar-accion :abrir [_]
  (let [dialogo (FileDialog. (Frame.) "Seleccionar Imagen" FileDialog/LOAD)]
    (.setFile dialogo "*.png;*.jpg;*.jpeg")
    (.setVisible dialogo true)
    (let [directorio (.getDirectory dialogo)
          archivo (.getFile dialogo)]
      (when (and directorio archivo)
        (manejar-accion :cargar (str directorio archivo))))))

;; Transforma imagenes con transparencia a RGB solido si se guardan como JPG para evitar errores
(defn guardar-archivo [imagen ruta]
  (let [ruta-baja (.toLowerCase ruta)
        ruta-final (if (or (.endsWith ruta-baja ".png")
                           (.endsWith ruta-baja ".jpg")
                           (.endsWith ruta-baja ".jpeg"))
                     ruta
                     (str ruta ".png"))
        ext (if (.endsWith (.toLowerCase ruta-final) "png") "png" "jpg")
        archivo (File. ruta-final)
        img-a-guardar (if (= ext "jpg")
                        (let [w (.getWidth imagen)
                              h (.getHeight imagen)
                              rgb-img (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
                              g (.createGraphics rgb-img)]
                          (.drawImage g imagen 0 0 nil)
                          (.dispose g)
                          rgb-img)
                        imagen)]
    (ImageIO/write img-a-guardar ext archivo)
    ruta-final))

;; Guarda la imagen sobreescribiendo el archivo actual
(defmethod manejar-accion :guardar [_]
  (let [{:keys [ruta imagen]} @estado]
    (when (and ruta imagen)
      (try
        (let [ruta-final (guardar-archivo imagen ruta)]
          (swap! estado assoc :ruta ruta-final)
          (println "Guardado en:" ruta-final))
        (catch Exception e
          (println "Error:" (.getMessage e)))))))

;; Abre un cuadro de dialogo para elegir el directorio y nombre del nuevo archivo
(defmethod manejar-accion :guardar-como [_]
  (let [{:keys [imagen]} @estado]
    (when imagen
      (let [dialogo (FileDialog. (Frame.) "Guardar Imagen Como" FileDialog/SAVE)]
        (.setFile dialogo "")
        (.setVisible dialogo true)
        (let [directorio (.getDirectory dialogo)
              archivo (.getFile dialogo)]
          (when (and directorio archivo)
            (try
              (let [ruta-final (guardar-archivo imagen (str directorio archivo))]
                (swap! estado assoc :ruta ruta-final)
                (println "Guardado como en:" ruta-final))
              (catch Exception e
                (println "Error:" (.getMessage e))))))))))