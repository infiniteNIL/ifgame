(ns example.core
  (:require [ifgame.core :as if]
            [ifgame.game :as game]
            [example.rooms]
            [example.objects]))

(defn -main []
  (let [g (game/create "Heidi"
                       "A Simple Example"
                       "Rod Schmidt"
                       :before-cottage)]
    (if/start-game g)))
