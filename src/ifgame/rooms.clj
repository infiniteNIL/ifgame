(ns ifgame.rooms)

(declare north-room)

(def start-room {:name "Starting Room"
                 :desc "You are in the starting room. A hallway leads north."
                 :exits {"north" #'north-room}})

(def north-room {:name "North Room"
                 :desc "Your are in the north room. A hallway leads south."
                 :exits {"south" #'start-room}})


(def east-room {:name "East Room"
                :desc "This is the east room. A hallway leads west."
                :exits {"west", #'start-room}})
