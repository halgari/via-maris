(ns via-maris.data-model.path-finding
  (:require [via-maris.db.util :as util]
            [via-maris.data-model.entities :as entities]
            [datomic.api :as d :refer [q]]
            [loom.graph :as graph]
            [loom.alg :as alg]))


(defrecord Jump [db from to]
  graph/Edge
  (src [this] from)
  (dest [this] to))

(defrecord SolarSystemGraph [db]
  graph/Graph
  (nodes [this]
    (->> (q '[:find ?id
              :where
              [?id :solarSystem/id]]
            db)
         (map first)))
  (edges [g]
    (->> (q '[:find ?from-id ?to-id
              :where
              [?id :jump/from ?from-id
               ?id :jump/to ?to-id]]
            db)
         (map (fn [[from to]]
                (->Jump db from to)))))
  (has-node? [g node]
    (if (d/entity db node)
      true
      false))
  (has-edge? [g n1 n2]
    (not (empty? (q '[:find ?jump-id
                      :in $ ?n1 ?n2
                      :where
                      [?jump-id :jump/from ?n1]
                      [?jump-id :jump/to ?n2]]
                    db n1 n2))))
  (out-degree [g node]
    (count (q '[:find ?jump-id
                :in $ ?node
                :where
                [?jump-id :jump/from ?node]])))
  (out-edges [g node]
    (->> (q '[:find ?to
              :in $ ?node
              :where
              [?jump-id :jump/from ?node]
              [?jump-id :jump/to ?to]])
         (map (fn [[to]]
                (->Jump db node to)))))
  (successors [g]
    (partial graph/successors g))
  (successors [g node]
    (->> (q '[:find ?to
              :in $ ?node
              :where
              [?jump-id :jump/from ?node]
              [?jump-id :jump/to ?to]]
            db
            node)
         (map first))))



(time (let [db (d/db (util/get-connection))]
        (let [path (alg/bf-path (->SolarSystemGraph db)
                                (:db/id (entities/solar-system db "Jita"))
                                (:db/id (entities/solar-system db "Rens")))]
          (map (comp (juxt :solarSystem/name
                           :solarSystem/security) (partial d/entity db)) path))
        ))
