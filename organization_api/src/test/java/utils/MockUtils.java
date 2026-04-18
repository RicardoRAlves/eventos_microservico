package utils;

import com.br.capoeira.eventos.organization_api.dto.*;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;

import java.util.List;

public class MockUtils {
    public static Organization getMockOrganization() {
        var organization = new Organization();
        organization.setId(1L);
        organization.setName("Grupo Bonfim");
        organization.setSlug("grupo-bonfim");
        organization.setDescription("Grupo de capoeira tradicional");
        organization.setLogoUrl("https://my-bucket.s3.amazonaws.com/logo.jpg");
        organization.setActive(true);
        return organization;
    }

    public static OrganizationCreateRequestDto getMockOrganizationCreateRequestDto() {
        var dto = new OrganizationCreateRequestDto();
        dto.setName("Grupo de Capoeira Nosso Senhor do Bonfim");
        dto.setSlug("bonfim-capoeira");
        dto.setDescription("Grupo tradicional de capoeira com atuação em diversas cidades.");
        dto.setLogoUrl("https://meusite.com/images/bonfim-logo.png");
        dto.setActive(true);

        dto.setMainUnit(getMockMainUnitDto());

        return dto;
    }

    public static MainUnitDto getMockMainUnitDto() {
        var dto = new MainUnitDto();
        dto.setName("Matriz Jundiaí");
        dto.setSlug("matriz-jundiai");
        dto.setDescription("Unidade principal do grupo.");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 123");
        dto.setContactPhone("+55 11 99999-9999");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUnit getMockOrganizationUnit() {
        var unit = new OrganizationUnit();
        unit.setId(1L);
        unit.setOrganization(getMockOrganization());
        unit.setName("Bonfim Jundiaí");
        unit.setSlug("bonfim-jundiai");
        unit.setDescription("Unidade de Jundiaí");
        unit.setCity("Jundiaí");
        unit.setState("SP");
        unit.setCountry("Brasil");
        unit.setAddress("Rua da Capoeira, 123");
        unit.setContactPhone("+55 11 99999-9999");
        unit.setContactEmail("jundiai@bonfim.com");
        unit.setActive(true);
        return unit;
    }

    public static OrganizationUnitDto getMockOrganizationUnitDto() {
        var dto = new OrganizationUnitDto();
        dto.setOrganizationId(1L);
        dto.setName("Bonfim Jundiaí");
        dto.setSlug("bonfim-jundiai");
        dto.setDescription("Unidade de Jundiaí");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 123");
        dto.setContactPhone("+55 11 99999-9999");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUpdateDto getMockOrganizationUpdateDto() {
        var dto = new OrganizationUpdateDto();
        dto.setId(1L);
        dto.setName("Grupo de Capoeira Nosso Senhor do Bonfim Atualizado");
        dto.setSlug("bonfim-capoeira");
        dto.setDescription("Grupo atualizado com novos eventos e unidades.");
        dto.setLogoUrl("https://meusite.com/images/bonfim-logo.png");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUnitUpdateDto getMockOrganizationUnitUpdateDto() {
        var dto = new OrganizationUnitUpdateDto();
        dto.setId(1L);
        dto.setName("Bonfim Jundiaí Atualizado");
        dto.setSlug("bonfim-jundiai");
        dto.setDescription("Unidade de Jundiaí atualizada com novos horários");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 456");
        dto.setContactPhone("+55 11 98888-8888");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationResponseDto getMockOrganizationResponseDto() {
        var dto = new OrganizationResponseDto();
        dto.setId(1L);
        dto.setName("Grupo de Capoeira Nosso Senhor do Bonfim");
        dto.setSlug("bonfim-capoeira");
        dto.setDescription("Grupo tradicional de capoeira com atuação em diversas cidades.");
        dto.setLogoUrl("https://meusite.com/images/bonfim-logo.png");
        dto.setActive(true);

        dto.setUnits(List.of(getMockOrganizationUnitSummaryDto()));

        return dto;
    }

    public static OrganizationUnitSummaryDto getMockOrganizationUnitSummaryDto() {
        var dto = new OrganizationUnitSummaryDto();
        dto.setId(1L);
        dto.setName("Matriz Jundiaí");
        dto.setSlug("matriz-jundiai");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setJoinCode("ABC123");
        dto.setActive(true);
        return dto;
    }

    public static OrganizationUnitResponseDto getMockOrganizationUnitResponseDto() {
        var dto = new OrganizationUnitResponseDto();
        dto.setId(1L);
        dto.setOrganizationId(1L);
        dto.setName("Bonfim Jundiaí");
        dto.setSlug("bonfim-jundiai");
        dto.setDescription("Unidade de Jundiaí");
        dto.setCity("Jundiaí");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setAddress("Rua da Capoeira, 123");
        dto.setContactPhone("+55 11 99999-9999");
        dto.setContactEmail("jundiai@bonfim.com");
        dto.setActive(true);
        return dto;
    }
}
