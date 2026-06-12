package br.com.lectek.copainsider.application.copa;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SelecaoLineupsData {

    private final Map<String, List<JogoConvocado>> lineups = new HashMap<>();

    public SelecaoLineupsData() {
        buildLineups();
    }

    public List<JogoConvocado> getLineup(String selecaoSlug) {
        return lineups.getOrDefault(selecaoSlug, List.of());
    }

    private void buildLineups() {
        lineups.put("brasil", List.of(
            c("brasil-alisson","Alisson","1","GL","brasil"),
            c("brasil-danilo","Danilo","2","DF","brasil"),
            c("brasil-marquinhos","Marquinhos","4","DF","brasil"),
            c("brasil-gabriel-magalhaes","Gabriel Magalhães","3","DF","brasil"),
            c("brasil-arana","Guilherme Arana","6","DF","brasil"),
            c("brasil-casemiro","Casemiro","5","MC","brasil"),
            c("brasil-bruno-guimaraes","Bruno Guimarães","8","MC","brasil"),
            c("brasil-paqueta","Lucas Paquetá","10","MC","brasil"),
            c("brasil-raphinha","Raphinha","19","AT","brasil"),
            c("brasil-vinicius-jr","Vinicius Jr.","7","AT","brasil"),
            c("brasil-endrick","Endrick","9","AT","brasil")
        ));

        lineups.put("argentina", List.of(
            c("argentina-martinez","E. Martínez","1","GL","argentina"),
            c("argentina-molina","Nahuel Molina","26","DF","argentina"),
            c("argentina-romero","Cristian Romero","13","DF","argentina"),
            c("argentina-l-martinez","Lisandro Martínez","25","DF","argentina"),
            c("argentina-tagliafico","Tagliafico","3","DF","argentina"),
            c("argentina-de-paul","Rodrigo De Paul","7","MC","argentina"),
            c("argentina-paredes","Leandro Paredes","5","MC","argentina"),
            c("argentina-enzo","Enzo Fernández","24","MC","argentina"),
            c("argentina-garnacho","Garnacho","11","AT","argentina"),
            c("argentina-messi","Lionel Messi","10","AT","argentina"),
            c("argentina-lautaro","Lautaro Martínez","22","AT","argentina")
        ));

        lineups.put("franca", List.of(
            c("franca-maignan","Mike Maignan","16","GL","franca"),
            c("franca-kounde","Jules Koundé","5","DF","franca"),
            c("franca-upamecano","Upamecano","4","DF","franca"),
            c("franca-saliba","William Saliba","17","DF","franca"),
            c("franca-hernandez","Theo Hernández","22","DF","franca"),
            c("franca-tchouameni","Tchouaméni","8","MC","franca"),
            c("franca-camavinga","Camavinga","10","MC","franca"),
            c("franca-griezmann","Griezmann","7","MC","franca"),
            c("franca-dembele","Ousmane Dembélé","11","AT","franca"),
            c("franca-mbappe","Kylian Mbappé","9","AT","franca"),
            c("franca-thuram","Marcus Thuram","15","AT","franca")
        ));

        lineups.put("portugal", List.of(
            c("portugal-rui-patricio","Rui Patrício","1","GL","portugal"),
            c("portugal-cancelo","João Cancelo","20","DF","portugal"),
            c("portugal-ruben-dias","Rúben Dias","6","DF","portugal"),
            c("portugal-antonio-silva","António Silva","4","DF","portugal"),
            c("portugal-nuno-mendes","Nuno Mendes","19","DF","portugal"),
            c("portugal-palhinha","João Palhinha","26","MC","portugal"),
            c("portugal-bernardo","Bernardo Silva","10","MC","portugal"),
            c("portugal-vitinha","Vitinha","8","MC","portugal"),
            c("portugal-rafael-leao","Rafael Leão","11","AT","portugal"),
            c("portugal-ronaldo","Cristiano Ronaldo","7","AT","portugal"),
            c("portugal-pedro-neto","Pedro Neto","17","AT","portugal")
        ));

        lineups.put("espanha", List.of(
            c("espanha-simon","Unai Simón","1","GL","espanha"),
            c("espanha-carvajal","Dani Carvajal","2","DF","espanha"),
            c("espanha-cubarsi","Pau Cubarsí","24","DF","espanha"),
            c("espanha-le-normand","Le Normand","5","DF","espanha"),
            c("espanha-cucurella","Marc Cucurella","3","DF","espanha"),
            c("espanha-rodri","Rodri","16","MC","espanha"),
            c("espanha-pedri","Pedri","8","MC","espanha"),
            c("espanha-fabian","Fabián Ruiz","7","MC","espanha"),
            c("espanha-yamal","Lamine Yamal","19","AT","espanha"),
            c("espanha-morata","Álvaro Morata","9","AT","espanha"),
            c("espanha-nico-williams","Nico Williams","11","AT","espanha")
        ));

        lineups.put("inglaterra", List.of(
            c("inglaterra-pickford","Jordan Pickford","1","GL","inglaterra"),
            c("inglaterra-walker","Kyle Walker","2","DF","inglaterra"),
            c("inglaterra-stones","John Stones","5","DF","inglaterra"),
            c("inglaterra-guehi","Marc Guéhi","6","DF","inglaterra"),
            c("inglaterra-trippier","Kieran Trippier","12","DF","inglaterra"),
            c("inglaterra-bellingham","Jude Bellingham","10","MC","inglaterra"),
            c("inglaterra-rice","Declan Rice","4","MC","inglaterra"),
            c("inglaterra-mainoo","Kobbie Mainoo","26","MC","inglaterra"),
            c("inglaterra-saka","Bukayo Saka","7","AT","inglaterra"),
            c("inglaterra-kane","Harry Kane","9","AT","inglaterra"),
            c("inglaterra-foden","Phil Foden","11","AT","inglaterra")
        ));

        lineups.put("alemanha", List.of(
            c("alemanha-neuer","Manuel Neuer","1","GL","alemanha"),
            c("alemanha-kimmich","Joshua Kimmich","6","DF","alemanha"),
            c("alemanha-rudiger","Antonio Rüdiger","2","DF","alemanha"),
            c("alemanha-tah","Jonathan Tah","4","DF","alemanha"),
            c("alemanha-mittelstadt","Mittelstädt","18","DF","alemanha"),
            c("alemanha-musiala","Jamal Musiala","10","MC","alemanha"),
            c("alemanha-wirtz","Florian Wirtz","19","MC","alemanha"),
            c("alemanha-gundogan","İlkay Gündoğan","21","MC","alemanha"),
            c("alemanha-sane","Leroy Sané","7","AT","alemanha"),
            c("alemanha-havertz","Kai Havertz","9","AT","alemanha"),
            c("alemanha-gnabry","Serge Gnabry","11","AT","alemanha")
        ));

        lineups.put("paises-baixos", List.of(
            c("paises-baixos-verbruggen","Verbruggen","23","GL","paises-baixos"),
            c("paises-baixos-dumfries","Denzel Dumfries","22","DF","paises-baixos"),
            c("paises-baixos-van-dijk","Virgil van Dijk","4","DF","paises-baixos"),
            c("paises-baixos-de-vrij","Stefan de Vrij","6","DF","paises-baixos"),
            c("paises-baixos-ake","Nathan Aké","5","DF","paises-baixos"),
            c("paises-baixos-koopmeiners","Koopmeiners","8","MC","paises-baixos"),
            c("paises-baixos-gravenberch","Gravenberch","16","MC","paises-baixos"),
            c("paises-baixos-reijnders","Tijjani Reijnders","14","MC","paises-baixos"),
            c("paises-baixos-simons","Xavi Simons","11","AT","paises-baixos"),
            c("paises-baixos-gakpo","Cody Gakpo","9","AT","paises-baixos"),
            c("paises-baixos-depay","Memphis Depay","10","AT","paises-baixos")
        ));

        lineups.put("italia", List.of(
            c("italia-donnarumma","Gianluigi Donnarumma","1","GL","italia"),
            c("italia-di-lorenzo","Giovanni Di Lorenzo","2","DF","italia"),
            c("italia-bastoni","Alessandro Bastoni","23","DF","italia"),
            c("italia-calafiori","Riccardo Calafiori","3","DF","italia"),
            c("italia-dimarco","Federico Dimarco","6","DF","italia"),
            c("italia-barella","Nicolò Barella","18","MC","italia"),
            c("italia-tonali","Sandro Tonali","4","MC","italia"),
            c("italia-frattesi","Davide Frattesi","16","MC","italia"),
            c("italia-chiesa","Federico Chiesa","14","AT","italia"),
            c("italia-scamacca","Gianluca Scamacca","9","AT","italia"),
            c("italia-pellegrini","Lorenzo Pellegrini","10","AT","italia")
        ));

        lineups.put("uruguai", List.of(
            c("uruguai-rochet","Sergio Rochet","1","GL","uruguai"),
            c("uruguai-nandez","Nahitan Nández","2","DF","uruguai"),
            c("uruguai-gimenez","José M. Giménez","3","DF","uruguai"),
            c("uruguai-olivera","Mathías Olivera","22","DF","uruguai"),
            c("uruguai-arauja","Ronald Araújo","4","DF","uruguai"),
            c("uruguai-valverde","Federico Valverde","15","MC","uruguai"),
            c("uruguai-bentancur","Rodrigo Bentancur","6","MC","uruguai"),
            c("uruguai-ugarte","Manuel Ugarte","5","MC","uruguai"),
            c("uruguai-de-la-cruz","N. De La Cruz","10","AT","uruguai"),
            c("uruguai-darwin","Darwin Núñez","9","AT","uruguai"),
            c("uruguai-pellistri","Facundo Pellistri","17","AT","uruguai")
        ));

        lineups.put("eua", List.of(
            c("eua-turner","Matt Turner","1","GL","eua"),
            c("eua-dest","Sergino Dest","2","DF","eua"),
            c("eua-richards","Chris Richards","4","DF","eua"),
            c("eua-robinson","Antonee Robinson","5","DF","eua"),
            c("eua-scally","Joe Scally","3","DF","eua"),
            c("eua-adams","Tyler Adams","4","MC","eua"),
            c("eua-mckennie","Weston McKennie","8","MC","eua"),
            c("eua-musah","Yunus Musah","6","MC","eua"),
            c("eua-pulisic","Christian Pulisic","10","AT","eua"),
            c("eua-pepi","Ricardo Pepi","9","AT","eua"),
            c("eua-weah","Tim Weah","11","AT","eua")
        ));

        lineups.put("mexico", List.of(
            c("mexico-ochoa","Guillermo Ochoa","13","GL","mexico"),
            c("mexico-sanchez","Jorge Sánchez","2","DF","mexico"),
            c("mexico-montes","César Montes","3","DF","mexico"),
            c("mexico-alvarez","Edson Álvarez","4","DF","mexico"),
            c("mexico-gallardo","Jesús Gallardo","23","DF","mexico"),
            c("mexico-herrera","Héctor Herrera","16","MC","mexico"),
            c("mexico-alvarado","Roberto Alvarado","6","MC","mexico"),
            c("mexico-guardado","Andrés Guardado","18","MC","mexico"),
            c("mexico-lozano","Hirving Lozano","22","AT","mexico"),
            c("mexico-jimenez","Raúl Jiménez","9","AT","mexico"),
            c("mexico-martin","Henry Martín","14","AT","mexico")
        ));

        lineups.put("japao", List.of(
            c("japao-gonda","Shuichi Gonda","1","GL","japao"),
            c("japao-sakai","Hiroki Sakai","19","DF","japao"),
            c("japao-itakura","Ko Itakura","5","DF","japao"),
            c("japao-taniguchi","Shogo Taniguchi","3","DF","japao"),
            c("japao-nagatomo","Yuto Nagatomo","5","DF","japao"),
            c("japao-endo","Wataru Endo","6","MC","japao"),
            c("japao-morita","Hidemasa Morita","15","MC","japao"),
            c("japao-ito","Junya Ito","14","MC","japao"),
            c("japao-minamino","Takumi Minamino","10","AT","japao"),
            c("japao-mitoma","Kaoru Mitoma","9","AT","japao"),
            c("japao-doan","Ritsu Doan","8","AT","japao")
        ));

        lineups.put("marrocos", List.of(
            c("marrocos-bono","Yassine Bounou","1","GL","marrocos"),
            c("marrocos-mazraoui","Noussair Mazraoui","2","DF","marrocos"),
            c("marrocos-dari","Achraf Dari","5","DF","marrocos"),
            c("marrocos-aguerd","Nayef Aguerd","3","DF","marrocos"),
            c("marrocos-hakimi","Achraf Hakimi","4","DF","marrocos"),
            c("marrocos-amrabat","Sofyan Amrabat","4","MC","marrocos"),
            c("marrocos-ounahi","Azzedine Ounahi","8","MC","marrocos"),
            c("marrocos-amallah","Selim Amallah","20","MC","marrocos"),
            c("marrocos-ziyech","Hakim Ziyech","7","AT","marrocos"),
            c("marrocos-en-nesyri","En-Nesyri","9","AT","marrocos"),
            c("marrocos-rahimi","Soufiane Rahimi","11","AT","marrocos")
        ));

        lineups.put("senegal", List.of(
            c("senegal-mendy","Édouard Mendy","16","GL","senegal"),
            c("senegal-sabaly","Youssouf Sabaly","2","DF","senegal"),
            c("senegal-koulibaly","Kalidou Koulibaly","3","DF","senegal"),
            c("senegal-diallo","Abdou Diallo","5","DF","senegal"),
            c("senegal-ballo-toure","Fodé Ballo-Touré","22","DF","senegal"),
            c("senegal-n-mendy","Nampalys Mendy","6","MC","senegal"),
            c("senegal-gueye","Idrissa Gueye","17","MC","senegal"),
            c("senegal-kouyate","Cheikhou Kouyaté","4","MC","senegal"),
            c("senegal-mane","Sadio Mané","10","AT","senegal"),
            c("senegal-sarr","Ismaila Sarr","7","AT","senegal"),
            c("senegal-dia","Boulaye Dia","14","AT","senegal")
        ));

        lineups.put("belgica", List.of(
            c("belgica-casteels","Koen Casteels","1","GL","belgica"),
            c("belgica-castagne","Timothy Castagne","3","DF","belgica"),
            c("belgica-faes","Wout Faes","4","DF","belgica"),
            c("belgica-theate","Theo Theate","5","DF","belgica"),
            c("belgica-saelemaekers","Alexis Saelemaekers","11","DF","belgica"),
            c("belgica-de-bruyne","Kevin De Bruyne","7","MC","belgica"),
            c("belgica-tielemans","Youri Tielemans","8","MC","belgica"),
            c("belgica-onana","Amadou Onana","15","MC","belgica"),
            c("belgica-doku","Jérémy Doku","11","AT","belgica"),
            c("belgica-lukaku","Romelu Lukaku","9","AT","belgica"),
            c("belgica-openda","Lois Openda","22","AT","belgica")
        ));
    }

    private JogoConvocado c(String slug, String nome, String num, String pos, String selecao) {
        return new JogoConvocado(slug, nome, num, pos, "", selecao);
    }
}
