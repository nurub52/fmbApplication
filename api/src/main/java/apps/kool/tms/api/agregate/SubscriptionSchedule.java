package apps.kool.tms.api.agregate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.DayOfWeek;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import apps.kool.tms.api.reqres.APIResponse;
import apps.kool.tms.api.utils.SectorName;
import apps.kool.tms.api.utils.WorkFlowStatus;
import lombok.Data;

@Data
@Document( collection= "subscriptionSchedule")
public class SubscriptionSchedule extends APIResponse {
	
	@JsonIgnore
    private @Id  ObjectId id;
	private String subscriberId; //hof
	private Map <DayOfWeek, Integer > optedSchedule;
	private SectorName zone;
	@DBRef
	private List<OverrideSubscriptionSchedule> overrideSchedules;
	@DBRef
	private TiffinPersonalization personalization; 
	
	private WorkFlowStatus status;
	
	private User user;
	private boolean isActive;
	
}
