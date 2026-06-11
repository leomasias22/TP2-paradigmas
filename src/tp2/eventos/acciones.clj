(ns tp2.eventos.acciones)

;; Enrutamiento de eventos dinamico basado en la accion
(defmulti manejar-accion (fn [accion & args] accion))

(defmethod manejar-accion :default [accion & args]
  (println "Accion no implementada:" accion))

(defmethod manejar-accion :salir [_ & _]
  (System/exit 0))