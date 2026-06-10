(ns tp2.eventos.estado)

;; Estado global del sistema gestionado de forma segura
(def estado (atom {:ruta nil
                   :imagen nil
                   :imagen-original nil
                   :filtro-seleccionado nil
                   :pipeline []
                   :filtros-aplicados []
                   :procesando? false}))