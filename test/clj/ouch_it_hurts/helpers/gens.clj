(ns ouch-it-hurts.helpers.gens
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as s]
            [clojure.spec.alpha :as spec]
            [ouch-it-hurts.specs :as sp]))

(defn inst->localdate [i]
  (-> (.toInstant i)
      (.atZone (java.time.ZoneId/systemDefault))
      (.toLocalDate)))

(defn gen-date [cnt from to]
  (->> (gen/sample (spec/gen (spec/inst-in from to)) 100)
       (drop (- 100 cnt))
       (map inst->localdate)))

(defn oms-gen []
  (s/join (gen/generate (gen/vector gen/nat sp/oms-length) 9)))

(defn m-gen []
  (let [first-name (gen/generate (spec/gen (spec/nilable #{"Иван" "Сергей" "Георгий" "Александр" "Михаил"})))
        last-name (gen/generate (spec/gen (spec/nilable #{"Петров" "Сидоров" "Иванов" "Александров" })))
        middle-name (gen/generate (spec/gen (spec/nilable #{"Иванович" "Сергеевич" "Георгиевич" "Александрович" "Михаилович"})))]
    {:first-name first-name
     :middle-name middle-name
     :last-name last-name}))

(defn f-gen []
  (let [first-name (gen/generate (spec/gen (spec/nilable #{"Светлана" "Екатерина" "Ксения" "Людмила" "Маргарита"})))
        last-name (gen/generate (spec/gen (spec/nilable #{"Петрова" "Сидорова" "Иванова" "Александрова"})))
        middle-name (gen/generate (spec/gen (spec/nilable #{"Ивановна" "Сергеевна" "Георгиевна" "Александровна" "Михаиловна"})))]
    {:first-name first-name
     :middle-name middle-name
     :last-name last-name}))

 (defn sex-gen []
   (gen/generate (spec/gen (spec/nilable sp/sex-enum))))

 (defn date-gen []
   (let [from #inst "1970"
         to #inst "2020"
         from-local-str (.toString (inst->localdate from))
         to-local-str (.toString (inst->localdate to))
         dates (gen-date 10 from to)]
     (gen/generate (spec/gen (spec/nilable (into #{} dates))))))

 (defn flat-gen []
   (gen/generate (spec/gen (into #{""} (mapv #(str "кв. " %)  (range 1 100))))))

 (defn building-gen []
   (gen/generate (spec/gen (into #{""} (mapv #(str "д. " %)  (range 1 100))))))

 (defn street-type-gen []
   (gen/generate (spec/gen #{"ул." "пл." "пр." "пер."})))

 (defn street-name-gen []
   (gen/generate (spec/gen #{"Ленина" "Пушкина" "Гоголя" "Булгакова" "Есенина" "Менделеева" "Попова" "Дзержинского"})))

 (defn city-gen []
   (gen/generate (spec/gen #{"Москва" "Санкт-Петербург" "Казань" "Нижний Новгород" "Екатеринбург" "Владивосток" "Калининград"})))

 (defn street-gen [] (str (street-type-gen) (street-name-gen)))

 (defn address-gen []
   (->> (range 10)
        (map (fn [_] (s/join ", " [(city-gen) (street-gen) (building-gen) (flat-gen)])) )
        (into #{})
        (spec/nilable)
        (spec/gen)
        (gen/generate)))

 (defn patient-gen []
   (-> (if-let [s (sex-gen)]
         (-> (if (= s "male") (m-gen) (f-gen)) (assoc :sex s))
         ((gen/generate (spec/gen #{m-gen f-gen}))))
       (assoc :address (address-gen))
       (assoc :oms (oms-gen))
       (assoc :birth-date (date-gen))))
