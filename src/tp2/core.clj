(ns tp2.core
  (:gen-class)
  (:require [tp2.app :as app])
  (:import [javax.swing SwingUtilities]))

(defn -main [& args]
  (SwingUtilities/invokeLater
    (fn []
      (app/crear-interfaz))))