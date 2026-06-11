(ns tp2.core
  (:gen-class)
  (:require [tp2.app :as app])
  (:import [javax.swing SwingUtilities]))

(defn -main [& args] (app/inicializar))
