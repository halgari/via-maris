(ns via-maris.db.util
  (:require [datomic.api :as d :refer [db q]]))


(def kw->attrs
  {:one [:db/cardinality :db.cardinality/one]
   :many [:db/cardinality :db.cardinality/many]
   :ref [:db/valueType :db.type/ref]
   :keyword [:db/valueType :db.type/keyword]
   :long [:db/valueType :db.type/long]
   :double [:db/valueType :db.type/double]
   :boolean [:db/valueType :db.type/boolean]
   :string [:db/valueType :db.type/string]
   :unique [:db/unique :db.unique/value]
   :indexed [:db/index true]})

(defn datoms-for-schema [desc]
  (map
    (fn [[id attrs]]
      (merge
        {:db/id                 (d/tempid :db.part/db)
         :db/ident              id
         :db.install/_attribute :db.part/db}
        (reduce
          (fn [m attr]
            (assert (kw->attrs attr) (pr-str attr))
            (apply assoc m (kw->attrs attr)))
          {}
          attrs)))
    desc))


(defn entity [& attrs]
  {:pre [(even? (count attrs))]}
  (let [id (d/tempid :db.part/user)]
    (for [[k v] (partition 2 attrs)
          :when (not (nil? v))]
      [:db/add id k v])))



#_(def uri "datomic:dev://localhost:4334/via_maris3")
(def uri "datomic:mem://via-maris")

(defn get-connection []
  (try
    (d/connect uri)
    (catch Exception ex
      (d/create-database uri)
      (d/connect uri))))

(defn delete-db []
  (d/delete-database uri))