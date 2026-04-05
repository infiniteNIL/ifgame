(ns ifgame.rooms
  (:require [ifgame.objects :as objects]))

#_(declare north-room)

#_(def start-room {:name "Starting Room"
                   :description "You are in the starting room. A hallway leads north."
                   :north #'north-room})

#_(def north-room {:name "North Room"
                   :description "Your are in the north room. A hallway leads south."
                   :south #'start-room})

#_(def east-room {:name "East Room"
                  :description "This is the east room. A hallway leads west."
                  :west #'start-room})

(def darkness {:name "Darkness"
               :description "It is pitch black, and you can't see a thing."})

(declare clearing top-of-tree forest)

(def before-cottage {:name "In front of a cottage"
                     :description "You stand outside a cottage. The forest stretches east."
                     :east #'forest
                     :has #{:light}})

(def starting-location before-cottage)

(def forest {:name "Deep in the forest"
             :description "Through the dense foliage, you glimpse a building to the west. A track heads to the northeast."
             :west #'before-cottage
             :northeast #'clearing
             :has #{:light}
             :objects [#'objects/bird]})

(def clearing {:name "A forest clearing"
               :description "A tall sycamore stands in the middle of this clearing. The path winds southwest through the trees."
               :southwest #'forest
               :up #'top-of-tree
               :has #{:light}
               :objects [#'objects/nest #'objects/tree]})

(def top-of-tree {:name "At the top of the tree"
                  :description "You cling precariously to the trunk."
                  :down #'clearing
                  :has #{:light}
                  :objects [#'objects/branch]})

#_(def test-room (room/map->Room {:name "Test Room"
                                  :description "This is a test room"}))