(ns ifgame.rooms
  "Defines all the rooms in the game"
  (:require [ifgame.room :refer [defroom]]))

(defroom :darkness
         "Darkness"
         "It is pitch black, and you can't see a thing.")

;; TODO: Add fn to each room that needs it for special processing
;; Processing order of objects functions:
;; 1) The actor
;; 2) The vehicle the actor is in
;; 3) The indirect object if any
;; 4) The direct object if any
;; 5) The verb
;; 6) The vehicle again
;; 7) The room the player is in
;; 8) Daemons that have no relation to the player's action
;;
;; If one handles it, the process of command is finished. A function may do something
;; but not handle the command
(defroom :before-cottage
         "In front of a cottage"
         "You stand outside a cottage. The forest stretches east."
          :east :forest
          :props #{:light})

(def starting-location :before-cottage)

(defroom :forest
         "Deep in the forest"
         "Through the dense foliage, you glimpse a building to the west. A track heads to the northeast."
         :west :before-cottage
         :northeast :clearing
         :props #{:light}
         ;:objects [#'objects/bird]})
         :objects [:bird])

(defroom :clearing
         "A forest clearing"
         "A tall sycamore stands in the middle of this clearing. The path winds southwest through the trees."
         :southwest :forest
         :up :top-of-tree
         :props #{:light}
         ;:objects [#'objects/nest #'objects/tree]
         :objects [:nest :tree])

(defroom :top-of-tree
         "At the top of the tree"
         "You cling precariously to the trunk."
         :down :clearing
         :props #{:light}
         ;:objects [#'objects/branch]
         :objects [:branch])
