(ns example.core
  (:require [ifgame.core :as if]
            [ifgame.game :as game]
            [example.rooms]      ;; needed to load rooms
            [example.objects]))  ;; needed to load objects

(defn -main []
  (let [g (game/create "Heidi"
                       "A Simple Example"
                       "Rod Schmidt"
                       :before-cottage)]
    (if/start-game g)))
