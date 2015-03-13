(ns via-maris.db.import
  (:require [via-maris.db.util :as util]
            [via-maris.db.schema :as schema]
            [datomic.api :refer [db q] :as d])
  (:import [java.sql DriverManager Statement Connection ResultSet]))


(defn result-set->map [^ResultSet rs]
  (let [md (.getMetaData rs)]
    (reduce
      (fn [acc idx]
        (assoc acc (keyword (.getColumnName md (int idx))) (.getObject rs (int idx))))
      {}
      (range 1 (inc (.getColumnCount md))))))

(defn get-table [sqlite-file table-name]
  (let [^Connection c (DriverManager/getConnection (str "jdbc:sqlite:" sqlite-file))
        ^Statement s (.createStatement c)
        sql (str "SELECT * FROM " table-name)
        rs (.executeQuery s sql)
        results (loop [acc []]
                  (if (.next rs)
                    (let [row (result-set->map rs)]
                      (recur (conj acc row)))
                    acc))]
    (.close rs)
    (.close s)
    (.close c)
    results))


(defn get-solar-system-data [sqlite-file]
  (let [rows (get-table sqlite-file "mapSolarSystems")
        datoms (->> (mapcat
                      (fn [row]
                        (util/entity :solarSystem/id (:solarSystemID row)
                                     :solarSystem/name (:solarSystemName row)
                                     :solarSystem/x (:x row)
                                     :solarSystem/y (:y row)
                                     :solarSystem/z (:z row)
                                     :solarSystem/xMin (:xMin row)
                                     :solarSystem/yMin (:yMin row)
                                     :solarSystem/zMin (:zMin row)
                                     :solarSystem/xMax (:xMax row)
                                     :solarSystem/yMax (:yMax row)
                                     :solarSystem/zMax (:zMax row)
                                     :solarSystem/luminosity (:luminosity row)
                                     :solarSystem/border (boolean (:border row))
                                     :solarSystem/fringe (boolean (:fringe row))
                                     :solarSystem/corridor (boolean (:corridor row))
                                     :solarSystem/hub (boolean (:hub row))
                                     :solarSystem/international (boolean (:international row))
                                     :solarSystem/regional (boolean (:regional row))
                                     :solarSystem/constellation (boolean (:constellation row))
                                     :solarSystem/security (:security row)
                                     :solarSystem/factionID (:factionID row)
                                     :solarSystem/radius (:radius row)
                                     :solarSystem/sunTypeID (:sunTypeID row)
                                     :solarSystem/securityClass (:securityClass row)))
                      rows))]
    datoms))

(defn get-solar-system-jumps [sqlite-file]
  (let [rows (get-table sqlite-file "mapSolarSystemJumps")
        datoms (->> (mapcat
                      (fn [row]
                        (util/entity :jump/fromSolarSystemID (:fromSolarSystemID row)
                                     :jump/toSolarSystemID (:toSolarSystemID row)))
                      rows))]
    datoms))

(defn get-links [db]
  (concat (->> (q '[:find ?link-id ?from-id
                    :where
                    [?link-id :jump/fromSolarSystemID ?id]
                    [?from-id :solarSystem/id ?id]]
                  db)
               (map (fn [[?link-id ?from-id]]
                      [:db/add ?link-id :jump/from ?from-id])))

          (->> (q '[:find ?link-id ?to-id
                    :where
                    [?link-id :jump/toSolarSystemID ?id]
                    [?to-id :solarSystem/id ?id]]
                  db)
               (map (fn [[?link-id ?from-id]]
                      [:db/add ?link-id :jump/to ?from-id])))))


(defn call-and-transact [conn f]
  (time @(d/transact conn (f))))

(defn perform-import [sqlite-file]
  (try (util/delete-db)
       (catch Exception ex))
  (let [conn (util/get-connection)]
    (schema/assert-schema conn)
    (call-and-transact conn (partial get-solar-system-data sqlite-file))
    (call-and-transact conn (partial get-solar-system-jumps sqlite-file))
    (call-and-transact conn (partial get-links (db conn)))
    nil))

(perform-import "/Users/tim/Downloads/Tiamat_1.0_110751_db/universeDataDx.db")
