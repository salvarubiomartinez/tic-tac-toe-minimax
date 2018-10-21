(ns tic-tac-toe.events
  (:require
   [re-frame.core :as re-frame]
   [tic-tac-toe.db :as db]
   ))

(defn turn-change [db]
  (assoc db :turn (if (= (:turn db) :X) :O :X)))

(defn play [db row col]
  (if (= " " (get-in db [:table row col]))
    (assoc-in db [:table row col] (if (= (:turn db) :X) "X" "O"))
    db))
(defn get-col [table x]
  [(get-in table [0 x]) (get-in table [1 x]) (get-in table [2 x])])

(defn get-col-table [table]
  [(get-col table 0) (get-col table 1) (get-col table 2)])

(defn one-row [row player]
  (every? (fn [x] (= x player)) row))

(defn cross-h? [table player]
  (let [values [(get-in table [0 0]) (get-in table [1 1]) (get-in table [2 2])]]
    (one-row values player)))

(defn cross-w? [table player]
  (let [values [(get-in table [0 2]) (get-in table [1 1]) (get-in table [2 0])]]
    (one-row values player)))

(defn rows? [table player]
  (reduce (fn [acc y] (if (= acc false) (one-row y player) true)) false table))

(defn get-winner [table]
  (let [col-table (get-col-table table)]
  (cond
    (rows? table "X") :X
    (rows? table "O") :O
    (rows? col-table "X") :X
    (rows? col-table "O") :O
    (cross-h? table "X") :X
    (cross-h? table "O") :O
    (cross-w? table "X") :X
    (cross-w? table "O") :O
    :else nil)))

(defn set-winner [db]
  (assoc db :winner (get-winner (:table db))))

(defn get-free-cells [table]
  (reduce (fn [accx x]
            (reduce (fn [accy y]
                      (if (= " " (get-in table [x y])) (cons [x y] accy) accy)) accx (range 3)
                    )) [] (range 3)))

(defn result [table]
  (cond
    (= (get-winner table) :X) 1
    (= (get-winner table) :O) -1
    (empty? (get-free-cells table)) 0
    :else nil))

(defn fill-table [table position player]
  (assoc-in table  [(position 0) (position 1)]  (if (= player :X) "X" "O")))

(defn minimax [table turn]
  (if (nil? (result table))
    (if (= turn :X)
      (apply max (map (fn [position] (minimax (fill-table table position :O) :O)) (get-free-cells table)))
      (apply min (map (fn [position] (minimax (fill-table table position :X) :X)) (get-free-cells table))))
    (result table)))

(defn rate-position [table position player]
  (minimax (fill-table table position player) player))

(defn ai-choose-move [table]
  (let [free-cells (get-free-cells table)
        ratings (map (fn [position] {
                                     :pos position
                                     :rating (rate-position table position :X)
                                     })
                     free-cells)]
    (println ratings)
    (:pos ( apply max-key :rating ratings ))
    )
  )

(defn ai-move [db]
  (if (= (:turn db) :X)
    (let [position (ai-choose-move (:table db))
          row (position 0)
          col (position 1)]
      (play db row col))
    db))



(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :name-change
 (fn [db [uno value]]
   ((.-log js/console) uno)
   (assoc db :name value)))

(re-frame/reg-event-db
 :re-start
 (fn [db [_ _]]
   (assoc db :table [
           [" " " " " "]
           [" " " " " "]
           [" " " " " "]]
          :winner nil
          :turn :O)))

(defn game-loop [db row col]
 ;;  (println (:table db))
 ;;  (println (get-free-cells (:table db)))
 ;;  (println (if (= (:turn db) :X) ( ai-choose-move (:table db) ) nil))
   (if (nil? (result (:table db)))
     (set-winner (turn-change (if (= (:turn db) :X)
        (ai-move db)
        (play db row col))))
     db))

(re-frame/reg-event-db
 :play
 (fn [db [_ row col]]
   (game-loop (game-loop db row col) 0 0)))

