package crm.example.facture.core.reglement;

import crm.example.facture.core.entreprise.Entreprise;
import crm.example.facture.core.entreprise.EntrepriseRepository;
import crm.example.facture.core.facture.Facture;
import crm.example.facture.core.facture.FactureRepository;
import crm.example.facture.core.facture.FactureServices;
import crm.example.facture.core.personnel.Personnel;
import crm.example.facture.core.personnel.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import crm.example.facture.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReglementServices {

    @Autowired
    ReglementRepository reglementRepository;

    @Autowired
    FactureRepository factureRepository;

    @Autowired
    PersonnelRepository personnelRepository;

    @Autowired
    EntrepriseRepository entrepriseRepository;

    public void reglement (){
        Facture ff = new Facture();
        ff.setIsregled(true);
        factureRepository.save(ff);
    }
    public ResponseEntity<?> creatReglement(Reglement reglement) {

        float montant = 0;
        if (!reglement.getFactures().isEmpty()) {
            if (reglement.getMonatant() != null && reglement.getFactures().size() == 1 && reglement.getType() == 1){
                montant = reglement.getMonatant();
                reglement();
                //System.out.println("momtant:" + montant);
            }
            else if (reglement.getMonatant() == null
                    && reglement.getFactures().size() > 1
                    && reglement.getType() == 2 ) {
                final float[] m = {0};
                reglement.getFactures().forEach(
                        facture -> {
                            Optional<Facture> facture1 = factureRepository.findById(facture.getId());
                            Facture f = facture1.get();
                            m[0] += f.getMontant();
                            reglement();
                            /*System.out.println("montant fact:" + f.getMontant());
                            System.out.println("m:" + m[0]);*/
                        });
                montant = m[0];
                /*System.out.println("m : "+m);
                System.out.println("m[0] : "+m[0]);
                System.out.println("montant: "+montant);*/
            }
        }
        //System.out.println(montant);
        reglement.setMonatant(montant);
        reglement = reglementRepository.save(reglement);
        return new ResponseEntity<>(reglement, HttpStatus.OK);
    }

    public List<Reglement> getReglements() {

        return reglementRepository.findAll();
    }

    public ResponseEntity<?> getOneReglement(int id) {
        Optional<Reglement> reglementOptional=reglementRepository.findById(id);

        if(!reglementOptional.isPresent()) {
            ErrorResponseModel errorResponseModel = new ErrorResponseModel ("reglement not found");
           return new ResponseEntity<>(errorResponseModel,HttpStatus.BAD_REQUEST);
        }

        Reglement reglement = reglementOptional.get();

        return new ResponseEntity<>(reglement, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteReglement(int id) {

        Optional<Reglement> reglementOptional = reglementRepository.findById(id);
        if(!reglementOptional.isPresent()) {
           ErrorResponseModel errorResponseModel = new ErrorResponseModel ("reglement not found");
            return new ResponseEntity<>(errorResponseModel,HttpStatus.BAD_REQUEST);
        }

        reglementRepository.deleteById(id);

        return new ResponseEntity<>( HttpStatus.OK);
    }

    public ResponseEntity<?> updateReglement(int id, Reglement reglement) {
        Optional<Reglement> reglementOptional= reglementRepository.findById(id);

        if(!reglementOptional.isPresent()){
            ErrorResponseModel errorResponseModel = new ErrorResponseModel ("reglement not found");
            return new ResponseEntity<>(errorResponseModel,HttpStatus.BAD_REQUEST);
        }

        Reglement dataBaseReglement= reglementOptional.get();



        if(reglement.getDelai() != null)
            dataBaseReglement.setDelai(reglement.getDelai());

        if(reglement.getMonatant() != null)
            dataBaseReglement.setMonatant(reglement.getMonatant());

        if(!reglement.getFactures().isEmpty())
        {
            List<Facture> fact = new ArrayList<Facture>();
            reglement.getFactures().forEach(
                    facture -> {
                        Optional<Facture> f = factureRepository.findById(facture.getId());
                        Facture ff = f.get();
                        fact.add(ff);
                    });
            System.out.println("fact" + fact);
            if(!fact.isEmpty())
                dataBaseReglement.setFactures(fact);
        }
        if( reglement.getPersonnels() != null)
        {
            Optional<Personnel> personnel = personnelRepository.findById(reglement.getPersonnels().getId());
            if (personnel != null){
                dataBaseReglement.setPersonnels(personnel.get());
            }
        }
        if( reglement.getEntreprises() != null)
        {
            Optional<Entreprise> entreprise = entrepriseRepository.findById(reglement.getEntreprises().getId());
            if (entreprise != null){
                dataBaseReglement.setEntreprises(entreprise.get());
            }
        }


        reglementRepository.save(dataBaseReglement);
        return new ResponseEntity<>(HttpStatus.OK);

    }

}

