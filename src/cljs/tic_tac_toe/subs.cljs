(ns tic-tac-toe.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::age
 (fn [db]
   (:age db)))

(re-frame/reg-sub
 ::table
 (fn [db]
   (:table db)))

(re-frame/reg-sub
 ::winner
 (fn [db]
   (:winner db)))

(re-frame/reg-sub
 ::width
 (fn [db]
   (:width db)))

(re-frame/reg-sub
 ::height
 (fn [db]
   (:height db)))
