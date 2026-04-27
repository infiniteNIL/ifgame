(ns example.objects
  (:require [ifgame.action :as action]
            [ifgame.object :refer [defobject]]))

;; TODO: Shouldn't be able to climb tree with more than 1 item (i.e. bird needs to be in nest)
;; TODO: Win game by putting nest on branch
(defobject :bird
           :names ["bird" "nestling"]
           :short-description "baby bird"
           :full-description "Too young to fly, the nestling tweets helplessly."
           :adjectives ["baby"]
           :props #{}
           :fn (fn [ast game]
                 (if (action/is-action? (:action ast) :take)
                   (do
                     (println "The bird pecks at your hand and you withdraw your hand quickly!")
                     true)
                   false)))

(defobject :nest
           :names ["nest" "twigs" "moss"]
           :short-description "bird's nest"
           :full-description "The nest is carefully woven of twigs and moss."
           :adjectives ["bird"]
           :props #{:container :open})

(defobject :tree
           :names ["sycamore" "tree"]
           :short-description "tall sycamore tree"
           :full-description "Standing proud in the middle of the clearing, the stout tree looks easy to climb."
           :adjectives ["tall" "stout" "proud"]
           :props #{:scenery})

(defobject :branch
           :names ["bough" "branch"]
           :short-description "wide firm bough"
           :full-description "It's flat enough to support a small object."
           :adjectives ["wide" "firm" "flat"]
           :props #{:static :supporter})
