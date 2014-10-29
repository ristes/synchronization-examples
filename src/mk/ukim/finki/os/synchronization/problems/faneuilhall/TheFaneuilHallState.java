package mk.ukim.finki.os.synchronization.problems.faneuilhall;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;


/**
 * 
 * @author Peeva Viki
 *
 */

public class TheFaneuilHallState extends AbstractState {

	private static final int JUDGE_INSIDE = 0;
	
	private static final String _5_DVAJCA_SUDII_SE_VO_SALATA = 
			"Two judges are in the hall";
	private static final String _5_NE_MOZE_SUDIJATA_DA_IZLEZE_KOGA_NE_E_VNATRE = 
			"There is no judge in the hall";
	private static final String _10_NE_MOZE_DOSELENIK_DA_VLEZE_KOGA_SUDIJATA_E_VNATRE = 
			"Immigrants can not enter while there is a judge in the hall";
	private static final String _5_NE_MOZE_DOSELENIK_DA_IZLEZE_KOGA_NEMA_DOSELENICI_VNATRE = 
			"There is no immigrants in the hall";
	private static final String _5_NE_MOZE_DA_SE_PRIJAVI_PRED_DA_VLEZE =
			"Immigrants can not check in if they are not inside";
	private static final String _5_NE_MOZE_DA_SEDNE_PRED_DA_SE_PRJAVI = 
			"Immigrants can not sit down if they are not checked in";
	private static final String _5_NE_MOZE_DA_JA_KAZE_ZAKLETVATA_AKO_NEMA_SEDNATO =
			"Immigrants can not swear if they haven't sat";
	private static final String _5_NE_MOZE_DVA_PATI_DA_SE_POVIKA_CONFIRM =
			"The judge can not confirm two times";
	private static final String _5_NE_MOZE_DVA_DOSELENICI_DA_GO_ZEMAT_POSLEDNIOT_SERTIFIKAT =
			"The last certificate can not be taken by two immigrants";
	private static final String _5_NE_MOZE_DA_ZEME_SERTIFIKAT_PRED_ZAKLETVATA =
			"Immigrants can not take the certificate before they swear";
	private static final String _10_NE_MOZE_DOSELENIK_DA_IZLEZE_KOGA_SUDIJATA_E_VNATRE = 
			"Immigrants can not leave the hall while there is judge inside";
	private static final String _5_MORA_SITE_DOSELENICI_DA_GO_POVIKAAT_CHECKIN =
			"The judge can not confirm if check in is not invoked by all immigrants that are inside";
	private static final String _5_NE_MOZE_GLEDAC_DA_IZLEZE_AKO_NEMA_GLEDACI_VNATRE =
			"There is no spectators in the hall";
	private static final String _10_NE_MOZE_ZAKLETVA_PRED_CONFIRM =
			"The judge must confirm before immigrants can swear ";
	private static final String _5_NE_MOZE_GLEDAC_DA_VLEZE_KOGA_SUDIJATA_E_VNATRE =
			"Spectators can not enter if the judge is inside the hall";
	private static final String _5_NE_MOZE_DA_GLEDA_AKO_NE_E_VNATRE =
			"Spectators can not speactate if they are not inside";
	private static final String _5_VLEGUVANJETO_NE_E_PARALELIZIRANO =
			"Entering the bulding is not parallelized";
	
	
	public TheFaneuilHallState(){
		
	}
	
	
	
	private BoundCounterWithRaceConditionCheck judgeInside = new BoundCounterWithRaceConditionCheck(
			0, 1, 5, _5_DVAJCA_SUDII_SE_VO_SALATA, 0, 5, _5_NE_MOZE_SUDIJATA_DA_IZLEZE_KOGA_NE_E_VNATRE);
	
	private BoundCounterWithRaceConditionCheck immigrantsInside = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DOSELENIK_DA_IZLEZE_KOGA_NEMA_DOSELENICI_VNATRE);
	
	private BoundCounterWithRaceConditionCheck readyEntered = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DA_SE_PRIJAVI_PRED_DA_VLEZE);
	
	private BoundCounterWithRaceConditionCheck readyCheckedIn = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DA_SEDNE_PRED_DA_SE_PRJAVI);
	
	private BoundCounterWithRaceConditionCheck readyToSwear = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DA_JA_KAZE_ZAKLETVATA_AKO_NEMA_SEDNATO);
	
	private BoundCounterWithRaceConditionCheck judgeConfirmed = new BoundCounterWithRaceConditionCheck(
			0, 1, 5, _5_NE_MOZE_DVA_PATI_DA_SE_POVIKA_CONFIRM, 0, 5, _10_NE_MOZE_ZAKLETVA_PRED_CONFIRM);
	
	private BoundCounterWithRaceConditionCheck readyCertificate = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DA_ZEME_SERTIFIKAT_PRED_ZAKLETVATA);
	
	private BoundCounterWithRaceConditionCheck spectatorsInside = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_GLEDAC_DA_IZLEZE_AKO_NEMA_GLEDACI_VNATRE);
	
	private BoundCounterWithRaceConditionCheck readyToSpectate = new BoundCounterWithRaceConditionCheck(
			0, null, 0, null, 0, 5, _5_NE_MOZE_DA_GLEDA_AKO_NE_E_VNATRE);
	
	private BoundCounterWithRaceConditionCheck checkingIn = new BoundCounterWithRaceConditionCheck(0);
	
	
	
	
	/*
	 * Ne smee da se vleguva dokolku sudijata e vo salata
	 * Ne e ogranicen brojot na doselenici koi moze da se vnatre vo salata
	 */
	public void enterImmigrant(){
		log(judgeInside.assertEquals(JUDGE_INSIDE, 10, _10_NE_MOZE_DOSELENIK_DA_VLEZE_KOGA_SUDIJATA_E_VNATRE), null);
		log(immigrantsInside.incrementWithMax(false),"Vleguva doselenik");
		log(readyEntered.incrementWithMax(false), null);
	}
	
	/*
	 * Samo od eden doselenik moze da bide povikan metodot checkIn
	 * Ne moze doselenik da se prijavi ako prethodno nema vlezeno vo salata
	 */
	public void checkIn(){
		log(readyEntered.decrementWithMin(false), "Doselenik se prijavuva");
		log(checkingIn.incrementWithMax(), null);
		log(readyCheckedIn.incrementWithMax(false), null);
		
	}
	
	/*
	 * Ne moze da sedne pred da se prijavi
	 */
	public void sitDown(){
		log(readyCheckedIn.decrementWithMin(false), "Doselenik sednuva");
		log(readyCheckedIn.incrementWithMax(false), null);
		log(readyToSwear.incrementWithMax(false), null);
		
	}
	
	/*
	 * Ne smee da se povika dokolku sudijata nema povikano confirm
	 * Ne moze da se povika pred doselenikot da sedne
	 */
	public void swear(){
		log(judgeConfirmed.decrementWithMin(), null);
		log(readyToSwear.decrementWithMin(false), "Doselenikot ja kazuva zakletvata");
		log(readyCheckedIn.decrementWithMin(false), null);
		log(readyCertificate.incrementWithMax(false), null);
		
	}
	
	/*
	 * Ne smee da se povika pred da se povika swear
	 */
	public void getCertificate(){
		log(readyCertificate.decrementWithMin(), "Doselenik zema sertifikat");
	}
	
	/*
	 * Ne smee da se izleguva dodeka sudijata e vo salata
	 * Ne smee da izleze ako prethodno nema vlezeno
	 */
	public void leaveImmigrant(){
		log(judgeInside.assertEquals(JUDGE_INSIDE, 10, _10_NE_MOZE_DOSELENIK_DA_IZLEZE_KOGA_SUDIJATA_E_VNATRE), null);
		log(immigrantsInside.decrementWithMin(false),"Izleguva doselenik");
		
	}
	
	/*
	 * Ne smee da ima poveke sudii vo salata istovremeno
	 */
	public void enterJudge(){
		log(judgeInside.incrementWithMax(), "Sudijata vleguva");
		
	}
	
	/*
	 * Ne smee da se povika dokolku site doselenici koi se vo salata nemaat povikano checkIn
	 */
	public void confirm(){
		log(checkingIn.assertEquals(immigrantsInside.getValue(), 5, _5_MORA_SITE_DOSELENICI_DA_GO_POVIKAAT_CHECKIN), null);
		log(null, "Sudijata povikuva confirm");
		checkingIn.setValue(0);
		judgeConfirmed.setValue(readyCheckedIn.getValue());
	}
	
	/*
	 * Ne moze sudijata da izleze ako nema sudija vnatre
	 */
	public void leaveJudge(){
		log(judgeInside.decrementWithMin(), "Sudijata izleguva");
	}
	
	/*
	 * Ne smee da se vleguva dokolku sudijata e vo salata
	 * Ne e ogranicen brojot na gledaci koi moze da se vnatre vo salata
	 */
	public void enterSpectator(){
		log(judgeInside.assertEquals(0, 5, _5_NE_MOZE_GLEDAC_DA_VLEZE_KOGA_SUDIJATA_E_VNATRE), null);
		log(spectatorsInside.incrementWithMax(false), "Vleguva gledac");
		log(readyToSpectate.incrementWithMax(false), null);
	}
	
	/*
	 * Ne e ogranicen brojot na gledaci koi moze da gledaat
	 * Ne moze da gleda ako ne e vnatre
	 */
	public void spectate(){
		log(readyToSpectate.decrementWithMin(false), "Gledacot ja gleda ceremonijata");
	}
	
	/*
	 * Ne moze da izleze ako ne e vnatre
	 */
	public void leaveSpectator(){
		log(spectatorsInside.decrementWithMin(false), "Izleguva gledac");
	}
	
	
	
	
	
	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

}
