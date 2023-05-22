package com.example.demo;

// import org.springframework.boot.SpringApplication;
// import java.io.*;
// import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ~~~~~~~~~~~~~
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@SpringBootApplication
public class DemoApplication {

	// ~~~~~ HTML->PDF
	// public static void main(String[] args) throws IOException {
	// SpringApplication.run(DemoApplication.class, args);
	// HtmlConverter.convertToPdf(new File("./pdf-input.html"), new
	// File("demo-html.pdf"));
	// }

	// ~~~~~ Signature
	public static final String DEST = "./";

	public static final String KEYSTORE = "./encrypt/ks";
	public static final String SRC = "./demo-html.pdf";

	public static final char[] PASSWORD = "password".toCharArray();

	public static final String RESULT_FILE = "hello_signed1.pdf";

	public void sign(String src, String dest, Certificate[] chain, PrivateKey pk,
			String digestAlgorithm,
			String provider, PdfSigner.CryptoStandard signatureType, String reason,
			String location)
			throws GeneralSecurityException, IOException {
		PdfReader reader = new PdfReader(src);
		PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), true);

		// Create the signature appearance
		Rectangle rect = new Rectangle(36, 348, 200, 100);
		PdfSignatureAppearance appearance = signer.getSignatureAppearance();
		appearance
				.setReason(reason)
				.setLocation(location)

				// Specify if the appearance before field is signed will be used
				// as a background for the signed field. The "false" value is the default value.
				.setReuseAppearance(false)
				.setPageRect(rect)
				.setPageNumber(1);
		signer.setFieldName("sig");

		IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm,
				provider);
		IExternalDigest digest = new BouncyCastleDigest();

		// Sign the document using the detached mode, CMS or CAdES equivalent.
		signer.signDetached(digest, pks, chain, null, null, null, 0, signatureType);
	}

	public static void main(String[] args) throws GeneralSecurityException,
			IOException {
		File file = new File(DEST);
		file.mkdirs();

		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(KEYSTORE), PASSWORD);
		String alias = ks.aliases().nextElement();
		PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
		Certificate[] chain = ks.getCertificateChain(alias);

		DemoApplication app = new DemoApplication();
		app.sign(SRC, DEST + RESULT_FILE, chain, pk, DigestAlgorithms.SHA256,
				provider.getName(),
				PdfSigner.CryptoStandard.CMS, "Test DoTQ", "DoTQ");
	}
}
