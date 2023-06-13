(ns ouch-it-hurts.events
  (:require [re-frame.core :as re :refer [reg-event-db inject-cofx]]
            [ouch-it-hurts.db :as db]))


(reg-event-db :initialize-db
              (fn [_ _] db/default-state))


(reg-event-db :add-filter
              (fn [db [_ field value]]
                (assoc-in db [:filters field] value)))

(reg-event-db :error
              (fn [db [_ msg]]
                (assoc db :error msg)))

(reg-event-db :store-patients-list
              (fn [db [_ patients]]
                (assoc db :patients patients)))

(reg-event-db :table/select-row
              (fn [db [_ id selected?]]
                (update-in db [:patients :data]
                           (fn [infos]
                             (case [id selected?]
                               [:all true] (map #(assoc % :selected? true) infos)
                               [:all false] (map #(dissoc % :selected?) infos)
                               (map #(if-not (= (:id %) id) %
                                             (assoc % :selected? selected?)) infos))))))

(defn- remove-empty [db key-path]
  (let [v (get-in db key-path)]
    (if (or (nil? v) (empty? v))
      (let [[before-last last] [(butlast key-path) (last key-path)]]
        (update-in db before-last  #(dissoc % last)))
      db)))


(reg-event-db :filters/error
              (fn [db [_ error]]
                (assoc db :filter-error error)))

(reg-event-db :filters/change
              (fn [db [_ key value]]
                (-> (assoc-in db [:filters key] value)
                    (remove-empty [:filters key]))))

(reg-event-db :filters/sex-opts
              (fn [db [_ opt checked?]]
                (if (= [opt checked?] [:all true]) (update db :filters #(dissoc % :sex-opts))
                  (let [op (if checked? clojure.set/union clojure.set/difference)
                        new-db (update-in db [:filters :sex-opts] #(op % #{opt}))]
                    (remove-empty new-db [:filters :sex-opts])))))

(reg-event-db :filters/birth-date-period
              (fn [db [_ key value]]
                (if value (assoc-in db [:filters :birth-date-period key] value)
                    (let [new-db (update-in db [:filters :birth-date-period] #(dissoc % key))]
                      (remove-empty new-db [:filters :birth-date-period])))))

(reg-event-db :close-modal #(dissoc % :modal))

(reg-event-db :show-modal
              (fn [db [_ form args]]
                (-> (if-let [a args] (assoc-in db [:modal :args] args) db)
                    (assoc-in [:modal :form] form))))


(reg-event-db :modal-args
              (fn [db [_ args]]
                (assoc-in db [:modal :args] args)))

