package apps.kool.tms.api.repository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import apps.kool.tms.api.agregate.OverrideSubscriptionSchedule;
import apps.kool.tms.api.agregate.SubscriptionSchedule;

public interface ISubscriberScheduleRepository {
	
   SubscriptionSchedule saveSubscriptionSchedule(SubscriptionSchedule subscriptionSchedule);
   
   List<SubscriptionSchedule> getAllSubscriptionSchedule();
   
   CompletableFuture<List<SubscriptionSchedule>> getAllSubscriptionScheduleAsync();
   
   CompletableFuture<List<SubscriptionSchedule>> getAllSubscriptionScheduleWithUserAsync();
   
   CompletableFuture<List<OverrideSubscriptionSchedule>> getOverrideScheduledForDateAsync(String selectedDate) throws Exception;
   
   SubscriptionSchedule getSubscriptionScheduleBySubscriberId (String subscriberId);
   
   OverrideSubscriptionSchedule updateOverrideSchedule (String subscriberId, OverrideSubscriptionSchedule overrideSubscriptionSchedule );
   
   SubscriptionSchedule updateSubscriptionSchedule (SubscriptionSchedule subscriptionSchedule );
   
   SubscriptionSchedule addSubscriptionSchedule (String subscriberId, OverrideSubscriptionSchedule overrideSubscriptionSchedule );
   
   boolean deleteOverrideSchedule(String subscriberId, String startDate) throws Exception ;
   
   List<OverrideSubscriptionSchedule> getOverrideScheduledForDate(String selectedDate) throws Exception;
   
   List<OverrideSubscriptionSchedule> getAllOverrideSubscriptionSchedule() ;
  
   
}
