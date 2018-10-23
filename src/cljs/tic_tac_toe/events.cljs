(ns tic-tac-toe.events
  (:require
   [re-frame.core :as re-frame]
   [tic-tac-toe.db :as db]))

(declare minimax)
(declare minimax-memo)
(declare rate-position)

(defn turn-change [db]
  (assoc db
         :turn (if (= (:turn db) :X)
                 :O
                 :X)))

(defn play [db row col]
  (if (nil? (get-in db [:table row col]))
    (assoc-in db
              [:table row col] (:turn db))
    db))

(defn get-col' [table x]
  [(get-in table [0 x])
   (get-in table [1 x])
   (get-in table [2 x])])

(defn get-col [table y]
  (map (fn [x] (get-in table [x y])) (range (count table))))

(defn get-col-table' [table]
  [(get-col table 0)
   (get-col table 1)
   (get-col table 2)])

(defn get-col-table [table]
  (map (fn [x] (get-col table x)) (range (count table))))

(defn one-row [row player]
  (every? (fn [x] (= x player)) row))

(defn cross-h? [table player]
  (let [values (map (fn [x] (get-in table [x x])) (range (count table)))]
    (one-row values player)))

(defn cross-w?
  [table player]
  (let [values (map (fn [x] (get-in table [x (- (count table) x 1)])) (range (count table)))]
    (one-row values player))) 

(defn rows? [table player]
  (reduce (fn [acc y]
            (if (= acc false)
              (one-row y player)
              true))
          false
          table))

(defn get-winner [table]
  (let [col-table (get-col-table table)]
    (cond
      (rows? table :X) :X
      (rows? table :O) :O
      (rows? col-table :X) :X
      (rows? col-table :O) :O
      (cross-h? table :X) :X
      (cross-h? table :O) :O
      (cross-w? table :X) :X
      (cross-w? table :O) :O
      :else nil)))

(defn set-winner [db]
  (assoc db
         :winner (get-winner (:table db))))

(defn get-free-cells [table]
  (reduce (fn [accx x]
            (reduce (fn [accy y]
                      (if (= nil (get-in table [x y]))
                        (cons [x y] accy)
                        accy))
                    accx
                    (range (count table))))
          (lazy-seq '())
          (lazy-seq (range (count (table 0))))))

(defn get-result [table]
  (cond
    (= (get-winner table) :X) 1
    (= (get-winner table) :O) -1
    (empty? (get-free-cells table)) 0
    :else nil))

(defn fill-table [table position player]
  (assoc-in table
            [(position 0)
             (position 1)]  player))

(defn take-until
  "Returns a lazy sequence of successive items from coll until
   (pred item) returns true, including that item. pred must be
   free of side-effects."
  [pred coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (if (pred (first s))
        (cons (first s) nil)
        (cons (first s) (take-until pred (rest s)))))))

(defn get-ratings [table player next-turn func cond]
    (apply func (take-until cond (lazy-seq (map
                 (fn [position]
                   (rate-position table position player next-turn))
                 (get-free-cells table))))))

(defn minimax [table next-turn]
  (let [result (get-result table)]
    (cond
      (some? result) result
      (= next-turn :X) (get-ratings table :X :O max (fn [x] (= x 1)))
      (= next-turn :O) (get-ratings table :O :X min (fn [x] (= x -1))))))

(def minimax-memo (memoize minimax))

(defn rate-position [table position player next-turn]
  ;;(println "rate position " position)
  (minimax-memo (fill-table table position player) next-turn))

(defn ai-choose-move [table]
  (let [free-cells (get-free-cells table)
        ratings (map
                 (fn [position]
                   {:pos position
                    :rating (rate-position table position :X :O)})
                 free-cells)]
    (println ratings)
    (:pos (apply max-key :rating ratings))))

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
   (assoc db
          :name value)))

(re-frame/reg-event-db
 :re-start
 (fn [db [_ _]]
   (assoc db
          :table (vec (repeat (:height db) (vec (repeat (:width db) nil))))
          :winner nil
          :turn :O)))

(defn game-loop [db row col]
  ;;  (println (:table db))
  ;;  (println (get-free-cells (:table db)))
  ;;  (println (if (= (:turn db) :X) ( ai-choose-move (:table db) ) nil))
  (if (nil? (get-result (:table db)))
    (set-winner
     (turn-change
      (if (= (:turn db) :X)
        (ai-move db)
        (play db row col))))
    db))

(re-frame/reg-event-db
 :play
 (fn [db [_ row col]]
   (game-loop
    (game-loop db row col)
    0
    0)))


;; (ai-choose-move [[:X nil :X] [nil :O :O] [nil nil :O]])
