package com.sublinks.sublinksapi.api.lemmy.v3.user.services;

import cn.apiclub.captcha.gimpy.BlockGimpyRenderer;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;
import cn.apiclub.captcha.gimpy.GimpyRenderer;
import cn.apiclub.captcha.gimpy.RippleGimpyRenderer;
import cn.apiclub.captcha.gimpy.ShearGimpyRenderer;
import cn.apiclub.captcha.gimpy.StretchGimpyRenderer;
import cn.apiclub.captcha.noise.CurvedLineNoiseProducer;
import cn.apiclub.captcha.noise.NoiseProducer;
import cn.apiclub.captcha.noise.StraightLineNoiseProducer;
import com.sublinks.sublinksapi.instance.models.LocalInstanceContext;
import com.sublinks.sublinksapi.person.Producers.LinesProducer;
import com.sublinks.sublinksapi.person.dto.Captcha;
import com.sublinks.sublinksapi.person.repositories.CaptchaRepository;
import jakarta.transaction.Transactional;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CaptchaService {

  private final CaptchaRepository captchaRepository;
  private final LocalInstanceContext localInstanceContext;

  private final Map<String, NoiseProducer> noiseProducers = Map.of(
      "Hard", new LinesProducer(3, List.of(Color.WHITE, Color.RED, Color.BLACK)),
      "Medium", new LinesProducer(2, List.of(Color.WHITE, Color.BLACK)),
      "Easy", new StraightLineNoiseProducer()
  );

  private final Map<String, GimpyRenderer> gimpyRenderer = Map.of(
      "Hard", new RippleGimpyRenderer(),
      "Medium", new RippleGimpyRenderer(),
      "Easy", new BlockGimpyRenderer()
  );
  private static String encodeToString(BufferedImage image) {

    String imageString;

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", bos);
      byte[] imageBytes = bos.toByteArray();

      imageString = Base64.getEncoder().encodeToString(imageBytes);

      bos.close();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Image could not be written");
    }

    return imageString;
  }

  @Transactional
  public Captcha getCaptcha() {

    Captcha captcha = captchaRepository.findFirstByLockedFalse().orElse(createCaptcha());

    captcha.setLocked(true);
    captchaRepository.save(captcha);

    return captcha;
  }

  public boolean validateCaptcha(String word, boolean remove) {

    Optional<Captcha> captcha = captchaRepository.findByWordIs(word);
    if (remove && captcha.isPresent()) {
      captchaRepository.delete(captcha.get());
    }
    return captcha.isPresent();
  }

  public Captcha createCaptcha() {


    NoiseProducer noiseProducer = noiseProducers.get(
        localInstanceContext.instance().getInstanceConfig().getCaptchaDifficulty());

    GimpyRenderer gimpyRender = gimpyRenderer.get(
        localInstanceContext.instance().getInstanceConfig().getCaptchaDifficulty());
    cn.apiclub.captcha.Captcha captcha = new cn.apiclub.captcha.Captcha.Builder(200, 50)
        .addText()
        .addBackground()
        .addNoise(noiseProducer)
        .gimp(gimpyRender)
        .build();

    Captcha captchaEntity = new Captcha();
    captchaEntity.setUuid(UUID.randomUUID().toString());
    captchaEntity.setWord(captcha.getAnswer());
    captchaEntity.setPng(encodeToString(captcha.getImage()));

    return captchaRepository.save(captchaEntity);
  }
}
