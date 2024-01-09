package apps.kool.tms.api.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import apps.kool.tms.api.agregate.OverrideSubscriptionSchedule;
import apps.kool.tms.api.agregate.SubscriptionSchedule;
import apps.kool.tms.api.agregate.TiffinPersonalization;
import apps.kool.tms.api.agregate.User;

@Repository
public class SubscriberScheduleRepository implements ISubscriberScheduleRepository {
	
	private final MongoTemplate mongoTemplate;
	
	@Autowired
	private ITiffinPersonalizationRepository personalizationRepository;  
	
    @Autowired
    public SubscriberScheduleRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public SubscriptionSchedule saveSubscriptionSchedule(SubscriptionSchedule subscriptionSchedule) {
		if(subscriptionSchedule != null && subscriptionSchedule.getOverrideSchedules()!=null){
			subscriptionSchedule.getOverrideSchedules().forEach(overrideSchedule -> mongoTemplate.save(overrideSchedule));
		}
		mongoTemplate.save(subscriptionSchedule);
		return subscriptionSchedule;
	}

	@Override
	public List<SubscriptionSchedule> getAllSubscriptionSchedule() {
		List<SubscriptionSchedule> subscriptionSchedules = mongoTemplate.findAll(SubscriptionSchedule.class);
//		subscriptionSchedules.forEach(subscriptionSchedule -> {
//			Query query = new Query();
//			query.addCriteria(Criteria.where("username").is(subscriptionSchedule.getSubscriberId()));
//			User user = mongoTemplate.findOne(query, User.class);
//			subscriptionSchedule.setUser(user);
//			subscriptionSchedule.setPersonalization(personalizationRepository.getPersonlizationBySubscriberId(subscriptionSchedule.getSubscriberId()));
//		});
		return subscriptionSchedules;	
//		 LookupOperation lookupOperation = LookupOperation.newLookup()
//               .from("user")
//                 .localField("subscriberId")
//                 .foreignField("username")
//                 .as("user");
//		Aggregation aggregation = Aggregation.newAggregation(lookupOperation);
//		
//		return mongoTemplate.aggregate(aggregation,"subscriptionSchedule", SubscriptionSchedule.class).getMappedResults();
	}

	@Override
	public SubscriptionSchedule getSubscriptionScheduleBySubscriberId(String subscriberId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("subscriberId").is(subscriberId));
		SubscriptionSchedule subscriptionSchedule = mongoTemplate.findOne(query, SubscriptionSchedule.class);
		return subscriptionSchedule;
	}

	@Override
	public OverrideSubscriptionSchedule updateOverrideSchedule(String subscriberId,OverrideSubscriptionSchedule overrideSubscriptionSchedule) {
		SubscriptionSchedule subscriptionSchedule = getSubscriptionScheduleBySubscriberId(subscriberId);
	    if(subscriptionSchedule.getOverrideSchedules().contains(overrideSubscriptionSchedule)) {
	    	subscriptionSchedule.getOverrideSchedules().stream().forEach(fetchedOverride -> {
	    		if(fetchedOverride.getOverrideEndDate().equals(overrideSubscriptionSchedule.getOverrideEndDate()) &&
	    				fetchedOverride.getOverrideStartDate().equals(overrideSubscriptionSchedule.getOverrideStartDate())){
	    			       fetchedOverride.setWeeklyOverrideSchedule(overrideSubscriptionSchedule.getWeeklyOverrideSchedule());
	    		}
	    	});
	    	mongoTemplate.save(subscriptionSchedule);
			return overrideSubscriptionSchedule;
	    }
	    return null;
	}
	
	
	public boolean deleteOverrideSchedule(String subscriberId, String startDate) throws Exception {
		Date dateStartDate =new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
		SubscriptionSchedule subscriptionSchedule = getSubscriptionScheduleBySubscriberId(subscriberId);
		if(subscriptionSchedule!=null) {
	    	Optional<OverrideSubscriptionSchedule> delOverrideSchedule = subscriptionSchedule.getOverrideSchedules().stream().filter(fetchedOverride -> 
	    							 fetchedOverride.getOverrideStartDate().equals(dateStartDate)				
				).findFirst();
	    	if(delOverrideSchedule.isPresent()){
	    		mongoTemplate.remove(new Query(Criteria.where("id").is(delOverrideSchedule.get().getId())), OverrideSubscriptionSchedule.class);
	    		subscriptionSchedule.getOverrideSchedules().remove(delOverrideSchedule.get());
	    		mongoTemplate.save(subscriptionSchedule);
		    	return true;
	    	}
	       	
	    }
		
	    return false;
	}


	@Override
	public SubscriptionSchedule updateSubscriptionSchedule(SubscriptionSchedule subscriptionSchedule) {
		Query query = new Query();
	    query.addCriteria(Criteria.where("subscriberId").is(subscriptionSchedule.getSubscriberId()));
	    SubscriptionSchedule subscriptionScheduleFromDB = mongoTemplate.findOne(query, SubscriptionSchedule.class);
	    TiffinPersonalization tiffinPersonalizationDB =  personalizationRepository.getPersonlizationBySubscriberId(subscriptionSchedule.getSubscriberId());
	    //modify and update with save()
	    tiffinPersonalizationDB.setNoRice(subscriptionSchedule.getPersonalization().getNoRice());
	    tiffinPersonalizationDB.setPackageType(subscriptionSchedule.getPersonalization().getPackageType());
	    subscriptionScheduleFromDB.setZone(subscriptionSchedule.getZone());
	    subscriptionScheduleFromDB.setOptedSchedule(subscriptionSchedule.getOptedSchedule());
	    mongoTemplate.save(subscriptionScheduleFromDB);
	    mongoTemplate.save(tiffinPersonalizationDB);
	   
	    //get the updated object again
	    SubscriptionSchedule subscriptionScheduleUpdated = mongoTemplate.findOne(query, SubscriptionSchedule.class);
		return subscriptionScheduleUpdated;
	}

	@Override
	public SubscriptionSchedule addSubscriptionSchedule(String subscriptionId,
			OverrideSubscriptionSchedule overrideSubscriptionSchedule) {
		SubscriptionSchedule subscriptionSchedule = getSubscriptionScheduleBySubscriberId(subscriptionId);
		overrideSubscriptionSchedule.setSubscriptionScheduleId(subscriptionSchedule.getId().toString());
		mongoTemplate.save(overrideSubscriptionSchedule);
		if(subscriptionSchedule.getOverrideSchedules()==null) {
			List<OverrideSubscriptionSchedule> overrideList = new ArrayList<OverrideSubscriptionSchedule>();
			overrideList.add(overrideSubscriptionSchedule);
			subscriptionSchedule.setOverrideSchedules(overrideList);
		} else {
			subscriptionSchedule.getOverrideSchedules().add(overrideSubscriptionSchedule);
		}
		mongoTemplate.save(subscriptionSchedule);
		
		return getSubscriptionScheduleBySubscriberId(subscriptionId);
	}

	@Override
	public List<OverrideSubscriptionSchedule> getOverrideScheduledForDate(String selectedDate) throws Exception {
		Date dateSelectedDate =new SimpleDateFormat("yyyy-MM-dd").parse(selectedDate);
		Query query = new Query();
		query.addCriteria(Criteria.where("overrideStartDate").lte(dateSelectedDate));
		query.addCriteria(Criteria.where("overrideEndDate").gte(dateSelectedDate));
		List<OverrideSubscriptionSchedule> overrideSubscriptionSchedules = mongoTemplate.find(query, OverrideSubscriptionSchedule.class);
		return overrideSubscriptionSchedules;
	}
	
	@Override
	public List<OverrideSubscriptionSchedule> getAllOverrideSubscriptionSchedule() {
		List<OverrideSubscriptionSchedule> overrideSubscriptionSchedules = mongoTemplate.findAll(OverrideSubscriptionSchedule.class);
		return overrideSubscriptionSchedules;	
	}

	@Override
	@Async
	public CompletableFuture<List<SubscriptionSchedule>> getAllSubscriptionScheduleAsync() {
		List<SubscriptionSchedule> subscriptionSchedules = mongoTemplate.findAll(SubscriptionSchedule.class);
	    return CompletableFuture.completedFuture(subscriptionSchedules);

	}
	
	@Override
	@Async
	public CompletableFuture<List<OverrideSubscriptionSchedule>> getOverrideScheduledForDateAsync(String selectedDate) throws Exception {
		Date dateSelectedDate =new SimpleDateFormat("yyyy-MM-dd").parse(selectedDate);
		Query query = new Query();
		query.addCriteria(Criteria.where("overrideStartDate").lte(dateSelectedDate));
		query.addCriteria(Criteria.where("overrideEndDate").gte(dateSelectedDate));
		List<OverrideSubscriptionSchedule> overrideSubscriptionSchedules = mongoTemplate.find(query, OverrideSubscriptionSchedule.class);
		return CompletableFuture.completedFuture(overrideSubscriptionSchedules);
	}
	
	@Override
	@Async
	public CompletableFuture<List<SubscriptionSchedule>> getAllSubscriptionScheduleWithUserAsync() {
			    //AggregationOperation unwind = Aggregation.unwind("studentIds");

	    String query1 = "{$lookup: {from: 'user',"
					    +"localField: 'subscriberId',"
					    +"foreignField: 'username',"
					    +"as: 'user'"
						+" }},"
						+"{$unwind: '$user'}";

	    TypedAggregation <SubscriptionSchedule> aggregation = Aggregation.newAggregation(
	    		SubscriptionSchedule.class,
	        new CustomAggregationOperation(query1)
	    );

	    AggregationResults<SubscriptionSchedule> results = mongoTemplate.aggregate(aggregation, SubscriptionSchedule.class);
	    return CompletableFuture.completedFuture(results.getMappedResults());

	}
	


}
