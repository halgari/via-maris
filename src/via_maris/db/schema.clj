(ns via-maris.db.schema
  (:require [via-maris.db.util :as util]
            [datomic.api :as d]))


(def solar-system-schema
  {:solarSystem/id #{:one :long :unique :indexed}
   :solarSystem/regionID #{:one :long :indexed}
   :solarSystem/constellationID #{:one :long :indexed}
   :solarSystem/name #{:one :string :unique :indexed}
   :solarSystem/x #{:one :double}
   :solarSystem/y #{:one :double}
   :solarSystem/z #{:one :double}
   :solarSystem/xMin #{:one :double}
   :solarSystem/yMin #{:one :double}
   :solarSystem/zMin #{:one :double}
   :solarSystem/xMax #{:one :double}
   :solarSystem/yMax #{:one :double}
   :solarSystem/zMax #{:one :double}
   :solarSystem/luminosity #{:one :double}
   :solarSystem/border #{:one :boolean}
   :solarSystem/fringe #{:one :boolean}
   :solarSystem/corridor #{:one :boolean}
   :solarSystem/hub #{:one :boolean}
   :solarSystem/international #{:one :boolean}
   :solarSystem/regional #{:one :boolean}
   :solarSystem/constellation #{:one :boolean}
   :solarSystem/security #{:one :double :indexed}
   :solarSystem/factionID #{:one :long}
   :solarSystem/radius #{:one :double}
   :solarSystem/sunTypeID #{:one :long}
   :solarSystem/securityClass #{:one :string :indexed}})

(def solar-system-jump
  {:jump/fromSolarSystemID #{:one :long :indexed}
   :jump/toSolarSystemID #{:one :long :indexed}
   :jump/from #{:one :ref}
   :jump/to #{:one :ref}})

(def schemas [solar-system-schema solar-system-jump])

(defn assert-schema [conn]
  (let [datoms (mapcat util/datoms-for-schema schemas)]
    @(d/transact conn datoms)))





