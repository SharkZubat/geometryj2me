package com.gd.j2me;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.MIDlet;

import org.bolet.jgz.GZipInputStream;

import net.sf.jazzlib.GZIPInputStream;

import com.sun.midp.io.Base64;
import com.tsg.hitbox.Direction;
import com.tsg.sharkutilitiesdemo.SharkUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class LevelGame extends GameCanvas implements Runnable {
    private long lastFrameTime = System.currentTimeMillis();
	public long currentFrameTime = System.currentTimeMillis();
    private long deltaTimeMillis = currentFrameTime - lastFrameTime;
    private double deltaTimeSeconds = deltaTimeMillis / 1000.0;
	
	private String levelData = "";
	public boolean isRunning;
	private Thread gameThread;
	
	//game level settings
    private int bgColor = 0x287dff;
    private int gnColor = 0x0066ff;
    
    //game level res
    private Image[] objImage = new Image[10];
    private Image bigFontBig;
	private int drewlayers;
	private float dirTest;
	private static GameObject[] gobjtest = new GameObject[5000];
	public int objsize = 1;
	private Player music;
	//PlayerScript curr_player = new PlayerScript();
	
	//camera
	float cameraX = 0;
	private float cameraY = 70;
	private int objcount = 0;
	
	private long lastFpsCheck = System.currentTimeMillis();
	private int currentFrames = 0;
	private int framesPerSecond = 0;
	private boolean isHolding;


	protected LevelGame(String levelData) {
		super(true);
		setFullScreenMode(true);
		// TODO Auto-generated constructor stub
		this.levelData = levelData;
	}
	
    public static String decompressGZIP(byte[] compressed) throws IOException {
        ByteArrayInputStream bai = new ByteArrayInputStream(compressed);
        GZipInputStream gzip = new GZipInputStream(bai);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzip.read(buffer)) > 0) {
            bao.write(buffer, 0, len);
        }
        
        gzip.close();
        bao.close();
        return new String(bao.toByteArray());
    }

	public void start() {
		// TODO Auto-generated method stub
	    //cameraX=curr_player.position.x+(getWidth()/6);
		String comprleveldata = "H4sIAAAAAAAAC41da5IuK27cUI-Dh0AQ_uU1eAG9AG_Bi3dV8VAqoe84ZuL0VX6SAKESICjqf_47t5_4K-E3_db0m39TKb_1N4YQfvU3_sbyG35je_5pv_F_4-_LF_vL9zB8fPFvvrAUxjj-pPFngPL7rzhU9KUiooqXZwhArdLvy3Ur8cou_3_WfGWN-WV9_83fv_-mJQWrd2lNeNWkf6um_qHm53_-K-af8P4p408df-Tn-Xf8tw5k_mnvn__O_aPS9-9Q8P3wX_L9O36NYfyJP-E_40_qP-nnafFPHv_Wn_ijP7G0n_ZAz4-l_DwSz_-fXx_2h-3xmUcwBxOsW7DXn7fE_hPbRS4-cu0RK-kTK6-at_yuL13nr699188fLY3osun40iVu-mtNkZdOm9RFpmB0zK94NXmRpa9-rZKtrwanb_xsZF2k2q9WmdrXzxHo2HfpQ0BlCWjaAh_5NVbL0j9oIXoYowIQZJorZW--JGau_NKlbzq9_6uJ6EJ087RGooVo9eW12b0_8hNno1NzNk2terK7Hpw_fz34aexU40417r7GOUSihWh1Fpu07N9j8PwxE12J7s4COaXDAjkVbHJObTc5Gq3r5xzXz5_CLL6CeTbop_78q4eNWjOK0VZNCV6LZKIr0d3rEzNLMXmgK9He8fIIC6avJPq9-fJLIf7m9Zfiu7HYo_TJV7JapW6u0euv0euvQjTYU0FghsfVd-qcO2t3XanT6NM3kmFfd369-ZWm5IRqvffpbckV05ont7tJMLp9QVEN0Lewtx6xGfgFr09Lj05pF0-qL2PQMVohEwmrlFdMggu3ErInqyctNETj1lXm_D3KLnMh2ZUZnbGeCOlJZztJ0ZPiyd3qOOjQl81GlYZ4VKvSRKqrUvZmyN4M2Zshd2fqxR2sjIHE7jp0FrI7VMTbQbwdxIJSsJ-__qxAV4s7amIIvw4rc9jOdzhcYb2iclfBqM5qzCdIxjzhqcAG9A6wBNFCtLJ8YAWBOFhix6AlUAu1zYAxO0EgM1AZ6GSJqcMaXjMDlYF-B1iCVXKZRCv_HggQUihUoFCFmEaFlYF8BZQklFTqGmGWgl72gzinfzlvlmosDaqFMtVkvv4aU0LoQC0MNOrRKWLdM0UAaHeAJVgl0UL8QvqYVi4vcIGBS2RAScLsPwX6tkuLZJcWqdUtUhkt-jKanwUsgV1mUy6TH9nGgadRGGl6pTMLHEAgQEkF08JFsAIxL-zBvHAimXy5B_LlnhmoZJzO0apztOocrTpHq06Ro1Nk6RR5er_SgQUOIHMRmevAgJCEkEqhSjCNCrvvwU6xp1Ns6uFGY6Mgur2GLYHCSgnFW3pxGF2IbjdaiFbmD6wwc4kHEAgQUsG0chGs4GPIUKm6ptwCYF7gJxn9c1qiEB1vtBCtzG9Vi0oCcqOVK8AKDmCveVaR2QBhgIJPSf7xKakSnYkORHtPn_JC8kLyTEObUmagMtCvgJKEkkqlMs1sKZCVUmagMgAP9Bc_p5IxKZ9IPpB6IN2QBmpS2UBmoDLQDfjal_mhyPTUZHqqMj35OV3pQIASvSe2S8DslQsDNEFZQCBAmYFo2U-6wGxAALAyhcbuIjR2Lw6rlfDzIvxUCz-lwk-pUGAQes5Fb3TkErgKxC_EL8RvdOFHrvjBpRR6Xgo9T6Xf6Ej8kfRFrgDRQvxC-oTKs54vO1FtaZUF5p1XEUCDZ6Vpy2qQDaaFY1CBkNFAcTeAQ0YtpLTyU1XpqarlRgvRkeSZtqey8iNW6RGrNMJDMvhNERR1yZCilnv_SFvE73X5BOUGxiv41U6ttJEr2PmGWSigX1Way6cUn3EuPuNcLOMcgf7yJGrKoIhqOrng7nIqpRdP-kTv_HknsKb0SJpNpBxIM-RVWoPLUNUgnvR5uUm3bEA0fQ2AvLq5RmfMGrMnqyfNmM3osQUC2ixFNgGo0SgAgcpA38CnMzmrV59er5ZeH9KDG9Qly4c2o4frfRqyN3H2Js5k4sFdTP_gR0A38DnCFFkZ_yre4uItLt7i4tOBUxhKG-KjNOAwDyrefMWbrzTfozPzbh1YKJ08gZ1O_rTU6GtZIT88GLxRq2VWFfhd9riqt5N6O_nce1Wyk3K2eCE7WzyspdVby-faa_PW8qn36pPm1SfNqyXNh2lnitxM2zl9vBCXPlafRVefRVefRdfg7bC4w8rsTgZMysYCuMRVmSlqeeZPv0-vq0-vq6XX1X6loD6ZAB01nap2TmIBpWzBxFX_nq-Jr-dLfVJffVJfLamvxsxVTLfRaIoC-rnQUrFyfJrDHhotZz1RuaKRUVrfa64M0LJoAUZXovONFuIX0se0Er-Svr0Unw17g3HZc6_JxGC9gf0GhukQNxu-48CCoUbDfaDKCHQG8tREIo1FWqEODATko0sHLFzJPrl9iZk76gS46zL31QC-UyGrd986jNMd4XnE5Hkg5D3Xka4nQpZedUJJvqMkh9juJElkECkM0MxUxS_2VArR6UYL8QvpY1qJX0mf-e4svzvnm5UkMHk3FRN_QdfVk7dQNRBodyBRTcEfp47GIgxktm5mc54AGzizRfNebWiJ1Mm4PTYAWikr7cgp7chpiTdaiF9IH9NK_Er6lJvgO3hWikD1IKiDzptnlwpVCIFI3TtFEoskFmks0liEgcyGyWz5zKY-ATY-OEDlOFh5DKs8hvEunlYaY2jbT2lbUGmfUmmfUmmfUmmfUmkbUmkbUmnPUGnPUGmfc9LQwTVQ99VAnVMrc3QG8h0oDBxKCystzNGYo3F_esffm6IOrDewexAKAbeqMFBBvTJ3AQLhLsK9nrmb8z5fo8pDlPIQxRuaSjuqSjuqSjuqSjuqSjuqSjuqSjuqqjREwXYpNsEbXW_jlrZLT-gat3x5CPDQozx8KQ9fysOX8milPFopj1bKo5XyaKU8Wmkjj1AerRTGs88mjUerxqMVb6oqbewqbfxqizdaiV9JH9NC_EL6jibsbs1Qqf4lPNEJ2hpHcJ7SBEA_V2k8MjUe3RqPbo1Ht8ZDVeOhqsFQlTK0cs5TPV9m42e2NgI86DVhIJJTdB7BeJdVcXP5q-7efoYEs67dXsx36tojvoD-MKDS5rDSbrPSlrjSlrnSlrrSZrLSZrPS7rbC7je08fUS8ql-W7f12wqv01pwLC_6XF7oWl60PE-c5_4uL8J_lOsCo6_B7GLLsQgEY_KI3Hnw7DxWdh6iOw-vHQbPiH7w4O9hf1-lHG8VzUIV5UGrBwZ4nOs8VnYeKzuMlWMd2Hkd-JqxvdZ-TB7lYvYvQfRmP1p4_rjsB8J6Rcca6ISt777cUxubVulLPb1JqjY3WGQL1p3ieckeIPpEREKePobg11MVgO2Jb56pw2HmQfcb_brWIWTgp16yB0fVJlrzyRoLcWVdBcCR6UEXotuNplqWcgPTBYQT0YMWovVGsxL5UzOBcOR50Jlo6hPtN5qV1huY_yyewEYWb-VGX4XID1q7-cFEvR-0Rn4wgLxcPYaknmMhVaeOBciqaQzZm_cBMgOVAbPwKAXXCIOl9rOtm6-4xm44Z7BWDHrxvQe9WPZB2624ntkcM-7pnMbEFraKzq3s3o9iDImBwkDzhokxEkcUBqzLKoiIVXoi1Vd6_ASnbBAA_SkwkBmoVAM4NhNRa7-YLZbL0_Wgl8frQS_PV4z1Eo0e9NbPsZJPjOopTEEdUg7EXDTqJQI9qF48aUrbFDqi0qjOcWO7RIwHvTa-XQ3Vrkbttwci9quh-s2oKVzi64PeDJECGSJ_aLQpYQKAUtgPXJmvXwU5Q79xStEvjTb7WiovSCss1ZgnMkss1EY_Md2aGKXNh-EYKT6TO1wv2bxm_1b-_k3dT6M7IjkVNiYfrbEMxmoO7zIsXO52FjYYBKRZS3G1HDpTol5OhQGfvVgA9mJq3GcpHTzlD-SUaqw5slBkGbBeSrduT-WKNo9iofkoNR_FnoiwkHBjxKqaI9k6CwNKxs-RzZgjGy3LgegfSDmkCvNEZolcduQWsZmzXFG9GT9HNmyWA9EDiWTrLAwoGV8C2Vo4UgqHROnUGxLYrpIP5AhocgQ9OQKjHMFT8oGEA-FQSe9yLi2RlUS2C3eWXEOsXEOs9FvHSuBOk3wg9UCOeAmvv6IaYS3CSqDvi98UfIAZ9SDXs0BKRmzYZyOWTuzVcgRBfKMedR0JmC1s4xQWHqVTdXK8VgeHFXgvV0BS8Oz8LgDqDG9ldyzR9o5jazO7M-8huOQZxkUEq0xLUsT8yuorG26yMEYXtbzDv5JyW6F_4ZXgCQgDSj5Cbw0vEQT8e9gxwau8E8gMVAY6FauBSlF-QJUfYVguD6WNm9945IHVbwEO1NFYBLo9o0wvK387JDtH2m6GG4JdnZ8lAGOh-VS35e_k48jeraO-qX4OFpUVAGFgn359AHeuajEAnYnep7TG3RSTYZ8si9kfnXroQnQjDVHUa_Anmx5aiN5nmyrw2ynbhSgDwsBYXy-t-7XnAfj3mR-aTJHJdPZKcwX-eVo6AMvX1AI8No5mOrD0zxyunAMIK2CO6gn1i1C_2LvSFX63FwkWhz0qi4Vm5xkOpkQsqr-2bYjUjuu2VYWNRkQ_aUGFWd6H4-m52YD-dmCPVl96Z2QhNtplOlPzb2ScYbI3byGnpTtdsl3qUoC2QJTpYMjSCOYuFMtzEQYoQ7ZqRRadKFp0HX3QXWG6RCbTLTK5kvvbvTEZfvexLq-zC24ekSuNFpmOLCwOMA4cchi3yEzVdi1NVnJ43Q4fEYgdbjV5QHoMYMtaARAGsCprX93q0sg7GnlHs9cOIjBYb_Tgq4XbeAqIMiAMQPDv1KWdurTbOXhXahYLaevN01UzwRciFZHAiDIgh8yu6wSS0YXo5nt3AnBv0dJZZdc1wttTikg-kMCIMiCHjNU--v6fDECrt_TS0K2uCd5MVkTqgeQDCYwoA3LIWO2TDwWSMtGV6E59kQL3xXrjz9qX4SiGAoItzo1bjO_cOSQwogzIIWMtzuRtmbwt0yRG1tuB1sLJUnYQECEnEHICoXu_ZL269yIJkG-NXy3HKwXWv4pIYgStie-IOSQfSGBEGZBDxqxZtv9EV99mtil-3imFXKpsl5OAAuPOCQXoy3cM4yzWtTO5tdXmtVfq3pqotAnY3QJbRzCt9u7YpKl_dfd_bADYqxKbJUA58-UwtXIa2abR42gvjk0V8-UvBa3zzS_UahcTBSjG5uXSyWqdrNbZavPFL4Fy55tfsl8bWmJ7qCvBm7H4l8Eeepsxwe98_87C9Q7LHYa57KjwwpMCpAYVRGzWOBFlQBiAHb4SUqaGuB-gJcMQ_p22h85EV6K7N1zkdwIR1isqV9Tmc9NCE_4sNHz9qcxeXs1WPT_74h1u5Y_KJ-98xb8f99Db-QrwQwckWtcXyKjPag-k7yV3WXsC5Csrwc2-MvBD_nSqUfluDgR7DQgIA-gu7ykQVzmHo4MPC-1k-D4VI3EdRWr_mKsq2Q07b65KwhINf6SqZqF-zFuVMFq9T66NAO9mK7t-Q4Wdb8AdHths74uOUoWeHKEnR-jJodcTN5ABsRcUIxYyPP4D6LTHA2QGaOQsygWPZFSxpvg3-R6angt7l2-6R8v508A-OQSxAXB53mhAV6pep961VwNnYT3oKgyeZXhlEMTiThjUQHaolIaqlLaqlLaqlqYaFalBd6tHvnS0egraW6xLtXlPHQktM3mlDFelDFeNZPI6MlxmhQX3_LnlrtMIlVMepgV1ZMDMgSulyCqlyCrc6TfKSlVXWdDpU8947fUD-MBMhQMzkyMzQMc0KrzINIDKInjjm2PZSRVBxrRf7H6QmknXAFDXQA5dH4y62pdGB10TAF0TYV0DRl09UL0mALomkt1hhdpTJ8EBoOBAuBIDhkoonNmJCJgu3ffpOF0TRl18lkozdVFEdFxRghDqKpR2VpuADxG49mFeCjwvhpCto1JGQNU_A0rTbVXKqUyGb3QOoCFu758itpRRuAp41msgcPuxv6bhoTPRlejum94oaT8BYQArMZEvwTQbN4rds2j1lzg8dCGaVrDapS9jfBzN38nw0EK0j-KTH5aKE1EGhAGYg81SIqykF9OXdZpcfN_Cg1QfpRvNjhvNjhvNjhtc-qCg4JtHLOQTqTarbXwRRGxzHmr1sInp1DIPTKwjsLEJBdAmFC9btYzTBGx50YDjmwvBEbe2Nvc2LKgwpu36TQMVoZmK0HAtYt__S0XMyU3aUaDhIdAPgA2s74T0Qoot3vscTLc9u42uk2OOhRkQORAlLTSAdhpAuw2gU0O2icLgoO2hTttDnbaHeib_eoB3OMgrNZ9mOTa-DTnat-m0b9PhjtsIDDsSdNqZ6LQz0Qs3tcCkQAHZuwiTb47ZFjI67Rl02zOYrVv3thoHWYlvn-94_bxOpa-FOlVGj1xDV8g1DGW0JdCVbKk0c3sYvt5IVNh8LUn249PxTSqHdBsYJ5SgQrQv0GlfoDcfYzu81jIBOOEGHDgeT2iMx4Ji0WaUnfYCOu0F9N6Jthl2A8BWCx0viHxlUvA5_IcuRLtZwUOPnMGOUhv5NvA6IuNjCGMrcIGWm0vBJ-AfWoj2l2AvBovsD9I6RoAUfFr8oTPRneht0jbpvDpzFjEQKCKTxTJZzPLQsQEDVjtnqraQKYRMIT4eLAYbbBdL27fxLCRaKYWMU8g4PsH70D79uIAIpZbA9Sh-0E-hkr0q2as2alu14Xoh5UBsCBpGhg9ITMAWsDIctbe8HzCHZJOaSMD1wEZRV1HWVZR1Fb3pKm5tkd5v2mxBcdC-OWojXteDqtcFRx77ALwjRbpTagPZCh9IzcBjY_fQ4nM1KfpcTYo-V5Oi-KculkS1KF_zsBYDqVamvz0qxeLH2AXoZqCboR6Am1HJOHY31KzEYEBTVCUddlVUBBq6BI5dIQB9ZNdLzULUHqGF-JnOYrGqQ1IqAtBNwielHprs2fziYwlY-IiWWZoWn9vbxkD27Oxs3WYxC1HfsGRZosGRQvcWTz7vlJLPO22B3ZDJAFFrqUCkUik-oZSSTyilRFPelFKklth09lszLWAvUx_A2yvRBHcBZYeBCYxDv8hivZT8HPihyVyZzJX97G4xwHCVYJ69EPbQWUzJ1tx5SkqMRxKZyObRU2beh4MyvXsTDECLKaGeERp_k1jubnAUigypsBcW6hmbjw-6spdWf3PgA5DZK5kdJtUVADug8vRsooasqxeMg5qu7JRKVxIuERtZEt5AMbT4CfBDkzEaGaOxOZs9CtOn4OrLxSPsQY07waazaQL77Sb3DvF3tDfEf9ouWbKwXSJL9LrRspJpjyDcguKQciCNEWFgjAsZEek_89VtZNRintGrjBrsXLXDyx3XO2xbM19xGe5gnkDxNZ8ANDfD4Q0EwEZTrZ03XzwRgMKA3w3cagvLnGqn6RXKzsqIvf86ROkC5AcQBuIVEBZBQK8ANkLkQOIfSDuk2qHZvC7DFdARhBBQBiJ3nciBKCMlkxq4O3kCfo9iiTgl8BYJCmEb6Y7npQaBcAWERQ4AzY2HdRzSDqmjctgBJbBd4MzPdMbZyO2MDs57NpX9gZh3yCU6ET3P5Cqcgd1SjCaPFtCQwZa1HEj6A5FDCpF2RySzlEPSH0g4pMKh-UCyHjXUoxU2r8j-6NBDC9GRaL1ZekoxGj06BvjJ_A2TBXSitVUOJP6ByCGFiN4RtPaQckj8AwmHVDg0Hwjaf9ZQj1bAw9R8Yu0BODrAtgwCbnholR40-m5ktt0j1JEPrWi8iSgjsu_w2UjG99F2dfxBig3LHdYrLHZuRxDOthFQoIWuReFoUf4DUUawYyeSWfMFCUfpR1ePgDxWOqtbmo9ZDV7Tqw76Zn81YQEBTEAvPnpkVKLDXWWIgAU6z2o6z2rgu6IICIsIKxUWweGplwM5Jku9cRP7MX3q6Wi0HXuKoEaZw5kF5n-oF32lH6G7H6G7H6G7H0NSP4aSfoT3foT3fgxJ_Rja-jG0dbq-bdeQnuFermjy6IdLoBnYBMB4EmhiKkEYiFdAWERYqbAIOIwEnpiuup1IO_Q0aFHkJgoDPOuUIIcVeGRZTTgQ6OiFHMZzPPEP5LCOvaG6S_edvOzMaPSoq9vRSyfi2ggT_7F0XGJzzFjrx-J_26aNNiGegN_LXhwwE5fIk12JNM9eQjC0Lima2k7YRlzx-_wP3a-0nzQtKT9pXa0LXLF8VBXNHGHEyVhJ6HC--2LV6-Th7o-3kX6WcKCB0O5s9oBvn5wJj398lfkv-XVC9J8OiCbxn714k3JEtytN3ZVuK4-lm2a-k3nsVDZU2qKzFt5skUDSIe1A0s3y6Ra1p7ShDcvdr_HtcuGxyhzZ4VYHBCz1L_4o7kML0bc1xeJilNcUWKRNQyUfkQ3vsEgg5ZB4M1am6Jex3XBoXhDPcDz6w-FuCwSEAXii4X4MBIQBsDVtSgltSi2arCr1iuYrGm49IPC6jmsN9AleP5GAxyFHOJIjHMk1xMg1SEm_9Z0E6Lt5eAh_yPBDDBlLjetR3Xj1-LBSoehCG3tS2vV3snW5xpxyjy7rUowdSyLo0G0RvpViCToEgktDKSmkh-1druGmULhZtpsl527lwMQ3Fiz6O5u1oGbQsEmlGEO7nlL1SpNl6zXmVIo5CVC0Wj0iTD0iTL1GmHqdX1W9W22WDFarclhtQmAi_72fh85EU-jQfqXJOHoNHXoNHUqhY3roYl4TqrEUntz7uy8LQGvqET_0iB96xA_lyQjoOtBrVFGOKthqiWLRI6OVrr_045dhlcaL1oWUA4EVRuNNioWUA4Fpb6PryTdSDqTZWEOb7UKb7QJvgAD_OJgWQcIh8OptAinsv3bEq3ZMhto1OrVrdGp_RKdZ8vecbaxcsGZYERQdQ7cgl-VepVPMopMF0vVK00PVrzGr8zwJVcS-296PKNaPKNaPKNavUaxfo1j_I4r1eFpylu0xPazbhUxZ_De8HjoTXYnuV9obcUkxeottqwYW2wIyNzd2TBQsOotyCMetWQTZd-o60FvcmjoNraA372upNjIWz6MfVp3zPhC72epe8xY6UFLoQEmJ7UqTLeNtMrR0-2F4omimyOFhlnLysHliu5knrpzeRAVRnOZPQ03dPFFc2g1XbEDLrrH-mPhDC9F6pclk6RYblm6aP07YYsME0GyJY8PkYUOmWySYdaS5-ETR-5Ke3pfk8L7JBt5Hp4IW7ZcqhV4se4DKgC3EGnAIAJ0BWswV_NYZFuMQ2LdBvXbZMxZ_oNmjCdrMps_XIJGvQSLf8jolQ5BAnQ6pB8J7SCVz3qrwF9i2HkQgGOaOTXomTjgHW23a8HSWqWC8QvMhcKnUBAoDidwAPozWQASBRm4gPIsr-EkxrAm6wf6Wmevw_UEzj7abG8ht_rPqy-gt3V-EV3yFP1O2eaCj-OtnS_vsOkTUnnj83tnoxkLxj86qFTqrtmiKdOUa_wqv5wKqaAzDXSggzuYq15BXbun0yZu1EB8k6wt_I2xLIcLbAKVwqr24z5MJNiAgBJsO2MpLjcC5KwdO-ILYBDoDRxDEr2mhEKw9Cn2JbLPAIwMfN2ug9wD8xym2po0OF6k0ktBBx0WTY9Xr-nbCOwMzaXaKeg3R9Rqi6zVE18BuVfOB1APhTf1SjxDNXwXbmpHnCNr8PbKNyCF1akaHrbgDL2gByzcW-CTYBAoDHJyVF9QFP7qFQuiM9K2yJYRAYoBX3IU_1bUrAy4NN8A1EEIgXQH28f31MY_SzdsJzUiupdeBQ68Dh8LaF6XRxfgrY5vnROSQ2h9PWjXgkyJlf7PMnRQp--tn67pdwcaBL8HXwBCIDKCj4Pe4GiDKAMcaOnlc6OTxoinWNBraEpSAE_R2TNkbL-dXc6kf23VEazSiYctg56jgB8FGjTvF1E4xtdMavfcrTXbo1zV6v8bnzuuBhsw7cR5RB6yHOucfC35WKkF1HHKs4_s1_neK_xn10U1ACz92nxYOy1Jsoy1iM9ZNYbsDK81HrRbstkfeH2qg4zM1FAYSA5ypnDIO4fxmDRTlK9zX6GQsYM-SBIByBSKLHADE64UU1nJB4PRADevypHdPGq9lWT_J3z_dvo2xfyv-N7TF_uzoQsAbZx0dwumSZXXvscuMjN62lpeG7X-jfpE2kyvccDmByAAf054yDoGDNwWkoMfh5swCMgcQWSSy0sgi6ACRDxut-p9IO6TaoRk6MnKkXy3wAWTqgjMgs7z3oRfotrjitQ80i5uG0sUeiyFrWJlqG1ZTChXEXhM5S_yh-FWlAVQGMgOBgc7usa4AZS0OCexC8NmoAmoQyAyEKxBZJLLSyMUeADpQqgfCJ_1XC8uhpx162iHVDs3giInHwKnHITwGLluRF6TbamdZ3nyjYsGyv2K4S85qsjxmr04hT8s8POXEDpDpSGvNNM1fMuVAGujl9M9C0oHY5xcfSNeXHUeQn_d2AXtkeWVADn1C6oZhc7u-ijVxmmvPJqKFM40WGeuQ7Zj2ujVhqTgq_Cr9P9YaZICQpQAA".replace('-', '+').replace('_', '/');
        try {
            byte[] compr = Base64.decode(comprleveldata);
            String decompr = decompressGZIP(compr);
            System.out.println("result: " + decompr);
        } catch (Exception e) {
            e.printStackTrace();
        }
		String[] input = GameObject.getImages();
		for (int i = 0; i < input.length; i++) {
			try {
				objImage[i+1] = Image.createImage("/img/obj/" + input[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//gobjtest = new GameObject[objsize];
		try {
			LevelLoader.Load(levelData);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e1.printStackTrace();
		}
		try {
			objImage[0] = Image.createImage("/img/obj/square_01_001.png");
			bigFontBig = Image.createImage("/img/fonts/bigFont-24.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/Jumper.mid");
	        music = Manager.createPlayer(is, "audio/midi");
	        music.prefetch();
	        music.start();
        	is.close();
        	is = null;
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
	}
	
	public void resetdelta() {
	    lastFrameTime = System.currentTimeMillis();
		currentFrameTime = System.currentTimeMillis();
	    deltaTimeMillis = currentFrameTime - lastFrameTime;
	    deltaTimeSeconds = deltaTimeMillis / 1000.0;
	}
	
	public void addobj(GameObject data) {
		gobjtest[objcount] = data;
		objcount++;
	}
	
	public void stop() {
		// TODO Auto-generated method stub
		isRunning = false;
		try {
			music.stop();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		// TODO Auto-generated method stub
	    long lastTime = System.currentTimeMillis();
	    final int TPS = 240;
	    final int SKIP_TICKS = 1000 / TPS;
	    long nextGameTick = System.currentTimeMillis();
		while (isRunning) {
	        long currentTime = System.currentTimeMillis();
	        long deltaTime = currentTime - lastTime;
	        lastTime = currentTime;
			controlcamera();
			while (currentTime > nextGameTick) {
				//curr_player.update(SKIP_TICKS / 1000.0f, isHolding);
				nextGameTick += SKIP_TICKS;
				currentTime = System.currentTimeMillis();
			}
			update();
			draw();
			updatefps();
			flushGraphics();
	        try {
	            Thread.sleep(Math.max(0, nextGameTick - currentTime));
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	private void controlcamera() {
		// TODO Auto-generated method stub
	    int keyState = getKeyStates();

	    if ((keyState & LEFT_PRESSED) != 0) {
	        cameraX-=deltaTimeSeconds*256f;
	    }
	    if ((keyState & RIGHT_PRESSED) != 0) {
	    	cameraX+=deltaTimeSeconds*256f;
	    }
	    if ((keyState & DOWN_PRESSED) != 0) {
	    	cameraY-=deltaTimeSeconds*256f;
	    }
	    if ((keyState & UP_PRESSED) != 0) {
	    	cameraY+=256f*deltaTimeSeconds;
	    }
	}
	public void freeup() {
		SharkUtilities.clearCaches();
		System.gc();
	}
	
    private void update() {
	    int keyState = getKeyStates();
    	currentFrameTime = System.currentTimeMillis();
        deltaTimeMillis = currentFrameTime - lastFrameTime;
        deltaTimeSeconds = deltaTimeMillis / 1000.0;
        
        drewlayers = 0;
        dirTest += -60 * deltaTimeSeconds;
        
        if (Runtime.getRuntime().freeMemory()/1024 <= Runtime.getRuntime().totalMemory()/1024/2) {
        	freeup();
        }
        
	    if (((keyState & FIRE_PRESSED) != 0) || ((keyState & KEY_NUM5) != 0) || ((keyState & KEY_NUM2) != 0)) {
	    	isHolding = true;
	    } else {
	    	isHolding = false;
	    }
        
        lastFrameTime = currentFrameTime;
    }
    
    private void renderobject(Image[] obj, GameObject gobj) {
    	int id = gobj.id;
    	float x = gobj.x;
    	float y = gobj.y;
    	boolean h = gobj.h;
    	boolean v = gobj.v;
    	Direction dir = gobj.dir;
    	
    	long calculatedX = (long) Math.floor((x-cameraX)/1.3636363636363636363636363636363636f+(getWidth()/2));
    	long calculatedY = (long) Math.floor((-y+cameraY)/1.36363636363636363636363636363636f+(getHeight()/2));
    	//calculatedX -= (long)deltaTimeSeconds*deltaTimeSeconds;
    	if (calculatedX > -20
    			&& calculatedX < getWidth()+20
    			&& calculatedY > -20
    			&& calculatedY < getHeight()+20) {
	    	if (drewlayers <= 100) {
		    	Graphics g = getGraphics();
		    	if (dir.toFloat() == 0) {
		    		SharkUtilities.drawImageWithAnchor(obj[id], (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	} else {
		    		SharkUtilities.drawImageWithDirAnchor(obj[id], dir.toFloat(), (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	}
		    	drewlayers++;
	    	}
    	}
    }
    
    private void updatefps() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsCheck >= 1000) {
            framesPerSecond = (int) (currentFrames);
            currentFrames = 0;
            lastFpsCheck = currentTime;
        }
    }

	
	private void draw() {
		Graphics g = getGraphics();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		//for (int i = 0; i < 4; i++) {
		//	for (int j = 0; j < 2; j++) {
		//		renderobject(objImage[0],i*30,j*30,dirTest);
		//	}
		//}
		
		for (int i = 0; i < objsize; i++) {
			try {
				System.out.println("rendering object" + gobjtest[i].id);
				renderobject(objImage,gobjtest[i]);
			} catch (Exception e) {
				System.out.println("rendering object fail" + gobjtest[i].id);
			}
		}
		//renderobject(objImage,new GameObject(1,curr_player.position.x,curr_player.position.y,false,false,curr_player.dir));
		
		CustomFont.drawString(bigFontBig, 0, 48, 0.5f, "FPS: " + (int)(framesPerSecond), 22, g);
		CustomFont.drawString(bigFontBig, 0, 60, 0.5f, "Drawn layers: " + drewlayers, 22, g);
		//CustomFont.drawString(bigFontBig, 0, 72, 0.5f, "RAM: " + Runtime.getRuntime().freeMemory()/1024 + "KB/" + Runtime.getRuntime().totalMemory()/1024 + "KB", 22, g);
		//CustomFont.drawString(bigFontBig, 0, 86, 0.5f, "CamX: " + (int)cameraX + "CamY:" + (int)cameraY, 22, g);
		currentFrames++;
	}
}
