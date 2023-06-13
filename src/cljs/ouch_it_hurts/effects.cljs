(ns ouch-it-hurts.effects
  (:require [re-frame.core :as re  :refer [reg-cofx reg-fx reg-event-fx]]
            [ouch-it-hurts.api :as api]))


(reg-fx :console (fn [str]
                   (js/console.log str)))

(reg-fx :fetch (fn [{:keys [filters sorting paging] :as all}]
                 (-> (api/fetch-patients-info all)
                     (.then (fn [infos] (re/dispatch [:store-patients-list infos])))
                     (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-fx :get-info-by-id
        (fn [[id dispatch]]
          (-> (api/get-patient-info-by-id id)
              (.then (fn [infos] (re/dispatch [dispatch infos])))
              (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-fx :add-info
        (fn [patient]
          (-> (api/add-patient-info patient)
              (.then (fn [infos] (re/dispatch [:fetch-patients-info])))
              (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-fx :restore-info
        (fn [id]
          (-> (api/restore-patient-info id)
              (.then (fn [infos] (re/dispatch [:fetch-patients-info])))
              (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-fx :delete-info
        (fn [ids]
          (-> (map api/delete-patient-info ids)
              (js/Promise.all)
              (.then (fn [infos] (re/dispatch [:table/paging-change {:page-number 1}])))
              (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-fx :edit-info
        (fn [patient-info]
          (-> (api/update-patient-info patient-info)
              (.then (fn [infos] (re/dispatch [:fetch-patients-info])))
              (.catch (fn [err]  (re/dispatch [:error (str err)]))))))

(reg-event-fx :fetch-patients-info
              (fn [{:keys [db]} _]
                {:fetch (select-keys db [:filters :sorting :paging])}))

(reg-event-fx :table/sorting-change
              (fn [{:keys [db]} [_ id sorting]]
                (let [new-db (if sorting (assoc-in db [:sorting id] sorting)
                                 (update db :sorting #(dissoc % id)))]
                  {:db new-db
                   :dispatch [:fetch-patients-info]})))

(reg-event-fx :table/paging-change
              (fn [{:keys [db]} [_ paging]]
                (let [new-db (update db :paging #(merge % paging))]
                  {:db new-db
                   :dispatch [:fetch-patients-info]})))

(reg-event-fx :filters/clean
              (fn [{:keys [db]} [_]]
                (let [new-db (-> (assoc db :filters {})
                                 (assoc :filter-error nil))]
                  {:db new-db
                   :dispatch [:table/paging-change {:page-number 1}]})))

(reg-event-fx :get-patient-by-id
              (fn [{:keys [db]} [_ id]]
                {:get-info-by-id [id :modal-args]}))

(reg-event-fx :add-patient
              (fn [{:keys [db]} [_ patient]]
                {:add-info patient
                 :dispatch [:close-modal]}))


(reg-event-fx :edit-patient
              (fn [{:keys [db]} [_ patient]]
                {:edit-info patient
                 :dispatch [:close-modal]}))

(reg-event-fx :delete-patient
              (fn [{:keys [db]} [_ ids]]
                {:delete-info ids
                 :dispatch [:close-modal]}))

(reg-event-fx :restore-patient
              (fn [{:keys [db]} [_ id]]
                {:restore-info id
                 :dispatch [:close-modal]}))
