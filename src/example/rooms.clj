(ns example.rooms
      (:require [ifgame.action :as action]
                [ifgame.game :as game]
                [ifgame.object :as object]
                [ifgame.room :refer [room]]
                [example.objects :as ex]))

(room :before-cottage
      "In front of a cottage"
      "You stand outside a cottage. The forest stretches east."
      :east :forest
      :props #{:light}
      :contents #{:cottage})

(room :forest
      "Deep in the forest"
      "Through the dense foliage, you glimpse a building to the west. A track heads to the northeast."
      :west :before-cottage
      :northeast :clearing
      :props #{:light}
      :contents #{:bird})

(room :clearing
      "A forest clearing"
      "A tall sycamore stands in the middle of this clearing. The path winds southwest through the trees."
      :southwest :forest
      :up :top-of-tree
      :props #{:light}
      :contents #{:nest :tree})

(defn nest-on-branch? [_game]
   (let [branch (object/get-object :branch)]
      (object/object-supports? branch :nest)))

(room :top-of-tree
      "At the top of the tree"
      "You cling precariously to the trunk."
      :down :clearing
      :props #{:light}
      :contents #{:branch}
      :handler (fn [ast game]
                  (cond
                     (action/is-action? (:action ast) :drop)
                     ;; After object has been dropped
                     (let [obj (:direct-object ast)]
                        ;; object already dropped. Need to move object from tree to clearing
                        (game/move-object (:do-key ast) :top-of-tree :clearing)
                        (println "The" (:name obj) "falls to the ground far below."))

                     (and (ex/nest-contains-bird? game) (nest-on-branch? game))
                     (do
                       (println "Congratulations! You have won the game.")
                       (game/won game))

                     :else
                     false)))
