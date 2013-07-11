(ns async-test.core
  (:use clojure.core.async))

(def philosophers {:socrates   [0 1]
                   :plato      [1 2]
                   :hickey     [2 3]
                   :sussman    [3 4]
                   :stroustrup [4 0]})

; fork is a thread that gives out a fork, waits for a fork and repeats.
(defn fork [ch]
  (>!! ch :fork)
  (<!! ch)
  (recur ch))

; start 5 fork threads
(defn set-table []
  (vec
    (for [ch [(chan) (chan) (chan) (chan) (chan)]]
      (do
        (thread (fork ch))
        ch))))

; make a channel with philo-1 buffer
; so that 4 can put sit down before it blocks
(defn make-butler []
  (chan (dec (count philosophers))))

(def table (set-table))
(def butler (make-butler))

; just for debugging
; channels are flow, not state
(def philostate (atom {:socrates   :thinking
                       :plato      :thinking
                       :hickey     :thinking
                       :sussman    :thinking
                       :stroustrup :thinking}))

; get and return forks from a fork thread
(def pickup <!!)
(def putdown #(>!! % :fork))

; to sit down means to fill the buffer
; to get up is to take out an item
(def sit-down #(>!! % :sit))
(def get-up <!!)

; zzzzzz
(defn wait [] (Thread/sleep (+ 1000 (rand-int 5000))))

(defn pickup-left [table philo]
  (pickup (table (first (philo philosophers)))))

(defn pickup-right [table philo]
  (pickup (table (second (philo philosophers)))))

(defn putdown-left [table philo]
  (putdown (table (first (philo philosophers)))))

(defn putdown-right [table philo]
  (putdown (table (second (philo philosophers)))))

(defn eat [table philo]
  (pickup-left table philo)
  (swap! philostate assoc philo :left-fork)
  (println @philostate)
  (wait)

  (pickup-right table philo)
  (swap! philostate assoc philo :eating)
  (println @philostate)
  (wait)

  (putdown-right table philo)
  (putdown-left table philo))

(defn philosopher [table butler philo]
  (sit-down butler)
  (swap! philostate assoc philo :sitting)
  (println @philostate)

  (eat table philo)

  (get-up butler)
  (swap! philostate assoc philo :thinking)
  (println @philostate)
  
  (wait)
  (recur table butler philo))

(defn run []
  (doseq [p (keys philosophers)]
    (thread (philosopher table butler p))))
