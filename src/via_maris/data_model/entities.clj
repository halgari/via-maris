(ns via-maris.data-model.entities
  (:require [via-maris.db.util :as util]
            [datomic.api :as d]
            [via-maris.util :as vmu]))



(defn solar-system [db system]
  (if (string? system)
    (d/entity db [:solarSystem/name system])
    (d/entity db [:solarSystem/id system])))

(defn solar-systems [db]
  (transduce
    (comp (map :e)
          (map (partial d/entity db)))
    conj
    (d/datoms db :avet :solarSystem/name)))
