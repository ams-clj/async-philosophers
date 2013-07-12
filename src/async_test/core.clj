(ns async-test.core
  (:use clojure.core.async))

(def philosophers {:socrates   [0 1]
                   :plato      [1 2]
                   :hickey     [2 3]
                   :sussman    [3 4]
                   :stroustrup [4 0]})

; fill the fork channels with forks
(defn set-table []
  (let [table [(chan 1) (chan 1) (chan 1) (chan 1) (chan 1)]]
    (doseq [ch table]
        (>!! ch :fork))
    table))

; make a channel with philo-1 buffer
; so that 4 can put sit down before it blocks
(defn make-butler []
  (chan (dec (count philosophers))))

; get the 2 forks fo a philosopher
(defn philo-forks [table philo]
  (map table (philo philosophers)))

; just for debugging
(defn philostate []
  (let [updates (chan 10)
        output (chan (sliding-buffer 10))]
    (go (loop [state {:socrates   :thinking
                      :plato      :thinking
                      :hickey     :thinking
                      :sussman    :thinking
                      :stroustrup :thinking}]
          (let [up (<! updates)
                new-state (apply assoc state up)]
            (>! output new-state)
            (recur new-state))))
    (go (while true
      (println (frequencies (vals (<! output))))))
    updates))

;(defn wait [] (Thread/sleep (+ 1000 (rand-int 5000))))
(defn wait [])

(defn philosopher [state table butler philo]
  (go (while true
    (let [fs (philo-forks table philo)]
      (>! butler :sit)
      (>! state [philo :sitting])

      (alts! fs)
      (>! state [philo :one-fork])

      (alts! fs)
      (>! state [philo :eating])
      (wait)

      (doseq [f fs]
        (>! f :fork))

      (<! butler)
      (>! state [philo :thinking])
      
      (wait)))))

(defn run []
  (let [table (set-table)
        butler (make-butler)
        state (philostate)]
    (doseq [p (keys philosophers)]
      (philosopher state table butler p))))
