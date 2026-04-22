(ns example.rooms
  (:require [ifgame.room :refer [defroom]]))

(defroom :before-cottage
         "In front of a cottage"
         "You stand outside a cottage. The forest stretches east."
         :east :forest
         :props #{:light})
