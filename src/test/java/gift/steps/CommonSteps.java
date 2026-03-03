package gift.steps;

import gift.auth.JwtProvider;
import gift.category.Category;
import gift.category.CategoryRepository;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.product.ProductRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Given("이름이 {string}인 카테고리가 등록되어 있고")
    public void 카테고리가_등록되어_있고(String name) {
        Category category = categoryRepository.save(new Category(name, "#000000", "", ""));
        context.storeId("categoryId", category.getId());
    }

    @And("{string} 상품이 가격 {int}, 이미지 {string}로 등록되어 있고")
    public void 상품이_등록되어_있고(String name, int price, String imageUrl) {
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        Product product = productRepository.save(new Product(name, price, imageUrl, category));
        context.storeId("productId", product.getId());
    }

    @And("재고 {int}개인 {string} 옵션이 등록되어 있고")
    public void 옵션이_등록되어_있고(int quantity, String optionName) {
        Long productId = ((Number) context.getId("productId")).longValue();
        Product product = productRepository.findById(productId).orElseThrow();
        Option option = optionRepository.save(new Option(product, optionName, quantity));
        context.storeOption(optionName, option);
    }

    @And("포인트 {int}을 가진 회원 {string}이 등록되어 있고")
    public void 회원이_등록되어_있고(int point, String email) {
        Member member = new Member(email, "password");
        member.chargePoint(point);
        memberRepository.save(member);
        String token = jwtProvider.createToken(email);
        context.storeMemberToken(email, token);
    }

    @Then("응답 상태 코드는 {int}이다")
    public void 응답_상태_코드는(int statusCode) {
        assertThat(context.getResponse().statusCode()).isEqualTo(statusCode);
    }
}
