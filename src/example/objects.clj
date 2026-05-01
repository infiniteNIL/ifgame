(ns example.objects
  (:require [ifgame.action :as action]
            [ifgame.game :as game]
            [ifgame.object :as object :refer [object]]))

(object :cottage
        "tiny cottage"
        ["cottage" "home" "house" "hut" "shed" "hovel"]
        ;:desc "It's small and simple, but you're very happy here."
        :props #{:scenery}
        :fn (fn [ast _game]
              (cond
                (action/is-action? (:action ast) :enter)
                (do
                  (println "It's such a lovely day -- much too nice to go inside.")
                  true)

                (action/is-action? (:action ast) :examine)
                (do
                  (println "It's small and simple, but you're very happy here.")
                  true)

                :else
                false)))

;; TODO: Shouldn't be able to climb tree with more than 1 item (i.e. bird needs to be in nest)
;; TODO: Win game by putting nest on branch
(object :bird
        "baby bird"
        ["bird" "nestling"]
        ;:desc "baby bird"
        ;:first-desc "Too young to fly, the nestling tweets helplessly."
        :adjectives ["baby"]
        :props #{}
        :fn (fn [ast _game]
              (cond
                (action/is-action? (:action ast) :examine)
                (do
                  (println "Too young to fly, the nestling tweets helplessly.")
                  true)

                (action/is-action? (:action ast) :listen)
                (do
                  (println "It sounds scared and in need of assistance.")
                  true)

                :else
                false)))

(object :nest
        "bird's nest"
        ["nest" "twigs" "moss"]
        ;:desc "bird's nest"
        ;:full-desc "The nest is carefully woven of twigs and moss."
        :adjectives ["bird"]
        :contains #{}
        :props #{:container :open}
        :fn (fn nest-handler [ast _game]
              (if (action/is-action? (:action ast) :examine)
                (do
                  (println "The nest is carefully woven of twigs and moss.")
                  true)
                false)))

(defn- nest-contains-bird? [_game]
  (let [nest (object/get-object :nest)]
    (object/object-contains? nest :bird)))

;; TODO: Don't let player climb tree (or up) if the bird is not in the nest
(object :tree
        "sycamore tree"
        ["sycamore" "tree"]
        ;:desc "tall sycamore tree"
        ;:first-desc "Standing proud in the middle of the clearing, the stout tree looks easy to climb."
        :adjectives ["tall" "stout" "proud"]
        :props #{:scenery}
        :fn (fn [ast game]
              (println "tree-action:" ast)
              (cond
                (and (action/is-action? (:action ast) :climb)
                     (game/in-inventory? game :bird)
                     (game/in-inventory? game :nest)
                     (not (nest-contains-bird? game)))
                (do (println "You start to climb the tree, but then realize you can't climb with both your hands full.")
                    true)

                (action/is-action? (:action ast) :examine)
                (do (println "Standing proud in the middle of the clearing, the stout tree looks easy to climb.")
                    true)

                :else
                false)))

;; TODO: When player puts nest (containing the bird) on branch, the player wins
(object :branch
        "branch"
        ["bough" "branch"]
        :desc "There is a wide firm bough here."
        ;:first-desc "It's flat enough to support a small object."
        :adjectives ["wide" "firm" "flat"]
        :props #{:static :supporter})
