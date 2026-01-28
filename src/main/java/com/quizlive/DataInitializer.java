package com.quizlive;

import com.quizlive.model.Block;
import com.quizlive.model.Question;
import com.quizlive.model.User;
import com.quizlive.repository.BlockRepository;
import com.quizlive.repository.QuestionRepository;
import com.quizlive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializa la base de datos con datos de ejemplo para pruebas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Comprobar si los datos ya existen
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping data creation");
            return;
        }

        log.info("Initializing database with sample data...");

        // Crear usuarios de prueba
        User host1 = createUser("host1", "password", "host1@quizlive.com");
        User host2 = createUser("host2", "password", "host2@quizlive.com");

        // Crear bloques con 25 preguntas cada uno
        Block block1 = createBlock("Matemáticas Básicas", "Preguntas de aritmética y álgebra", host1);
        Block block2 = createBlock("Historia Mundial", "Eventos históricos importantes", host2);

        // Añadir 25 preguntas al bloque 1
        addMathQuestions(block1);

        // Añadir 25 preguntas al bloque 2
        addHistoryQuestions(block2);

        log.info("Database initialized successfully!");
        log.info("Sample users created:");
        log.info("  - Username: host1, Password: password");
        log.info("  - Username: host2, Password: password");
    }

    private User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("HOST");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Block createBlock(String name, String description, User owner) {
        Block block = new Block();
        block.setName(name);
        block.setDescription(description);
        block.setOwner(owner);
        return blockRepository.save(block);
    }

    private void addMathQuestions(Block block) {
        String[][] questions = {
            {"¿Cuánto es 2 + 2?", "3", "4", "5", "6", "2"},
            {"¿Cuánto es 5 × 3?", "12", "15", "18", "20", "2"},
            {"¿Cuánto es 10 - 7?", "2", "3", "4", "5", "2"},
            {"¿Cuánto es 20 ÷ 4?", "4", "5", "6", "7", "2"},
            {"¿Cuánto es 3²?", "6", "9", "12", "15", "2"},
            {"¿Cuánto es 50% de 100?", "25", "50", "75", "100", "2"},
            {"¿Cuánto es 8 + 12?", "18", "20", "22", "24", "2"},
            {"¿Cuánto es 7 × 7?", "42", "49", "56", "63", "2"},
            {"¿Cuánto es 100 - 45?", "45", "55", "65", "75", "2"},
            {"¿Cuánto es 36 ÷ 6?", "4", "5", "6", "7", "3"},
            {"¿Cuánto es 4³?", "12", "16", "48", "64", "4"},
            {"¿Cuánto es 25% de 200?", "25", "50", "75", "100", "2"},
            {"¿Cuánto es 15 + 23?", "35", "38", "40", "42", "2"},
            {"¿Cuánto es 9 × 6?", "48", "52", "54", "56", "3"},
            {"¿Cuánto es 81 - 17?", "62", "64", "66", "68", "2"},
            {"¿Cuánto es 144 ÷ 12?", "10", "12", "14", "16", "2"},
            {"¿Cuánto es 5²?", "10", "15", "20", "25", "4"},
            {"¿Cuánto es 75% de 80?", "40", "50", "60", "70", "3"},
            {"¿Cuánto es 32 + 18?", "48", "50", "52", "54", "2"},
            {"¿Cuánto es 12 × 5?", "50", "55", "60", "65", "3"},
            {"¿Cuánto es 90 - 34?", "52", "54", "56", "58", "3"},
            {"¿Cuánto es 64 ÷ 8?", "6", "7", "8", "9", "3"},
            {"¿Cuánto es 10²?", "50", "100", "150", "200", "2"},
            {"¿Cuánto es 30% de 150?", "35", "40", "45", "50", "3"},
            {"¿Cuánto es 45 + 55?", "90", "95", "100", "105", "3"}
        };

        for (String[] q : questions) {
            createQuestion(block, q[0], q[1], q[2], q[3], q[4], Integer.parseInt(q[5]));
        }
    }

    private void addHistoryQuestions(Block block) {
        String[][] questions = {
            {"¿En qué año se descubrió América?", "1482", "1492", "1502", "1512", "2"},
            {"¿Quién fue el primer presidente de USA?", "Jefferson", "Washington", "Lincoln", "Adams", "2"},
            {"¿En qué año cayó el Muro de Berlín?", "1987", "1988", "1989", "1990", "3"},
            {"¿Quién pintó la Mona Lisa?", "Michelangelo", "Da Vinci", "Raphael", "Donatello", "2"},
            {"¿En qué año comenzó la Segunda Guerra Mundial?", "1937", "1938", "1939", "1940", "3"},
            {"¿Quién fue Napoleón Bonaparte?", "Rey español", "Emperador francés", "Zar ruso", "Kaiser alemán", "2"},
            {"¿En qué año llegó el hombre a la Luna?", "1967", "1968", "1969", "1970", "3"},
            {"¿Quién escribió Don Quijote?", "Lope de Vega", "Cervantes", "Góngora", "Quevedo", "2"},
            {"¿En qué siglo vivió Cleopatra?", "Siglo I a.C.", "Siglo I d.C.", "Siglo II a.C.", "Siglo II d.C.", "1"},
            {"¿Dónde se firmó la Declaración de Independencia de USA?", "Nueva York", "Boston", "Filadelfia", "Washington", "3"},
            {"¿Cuándo fue la Revolución Francesa?", "1769", "1779", "1789", "1799", "3"},
            {"¿Quién descubrió la penicilina?", "Pasteur", "Fleming", "Koch", "Curie", "2"},
            {"¿En qué año terminó la Primera Guerra Mundial?", "1916", "1917", "1918", "1919", "3"},
            {"¿Quién fue Julio César?", "Filósofo griego", "General romano", "Rey persa", "Faraón egipcio", "2"},
            {"¿Dónde se construyó la Torre Eiffel?", "Londres", "Berlín", "París", "Roma", "3"},
            {"¿En qué año se hundió el Titanic?", "1910", "1911", "1912", "1913", "3"},
            {"¿Quién fue Galileo Galilei?", "Pintor", "Astrónomo", "Músico", "Poeta", "2"},
            {"¿Cuándo comenzó la Edad Media?", "Siglo III", "Siglo V", "Siglo VII", "Siglo IX", "2"},
            {"¿Quién fue Simón Bolívar?", "Explorador", "Libertador", "Escritor", "Científico", "2"},
            {"¿En qué país nació Mozart?", "Alemania", "Austria", "Italia", "Francia", "2"},
            {"¿Cuándo se firmó la Carta Magna?", "1115", "1215", "1315", "1415", "2"},
            {"¿Quién inventó la imprenta?", "Galileo", "Gutenberg", "Newton", "Edison", "2"},
            {"¿En qué año se fundó Roma?", "653 a.C.", "703 a.C.", "753 a.C.", "803 a.C.", "3"},
            {"¿Quién fue Alejandro Magno?", "Filósofo", "Conquistador", "Artista", "Comerciante", "2"},
            {"¿Dónde se encuentra Machu Picchu?", "México", "Colombia", "Perú", "Chile", "3"}
        };

        for (String[] q : questions) {
            createQuestion(block, q[0], q[1], q[2], q[3], q[4], Integer.parseInt(q[5]));
        }
    }

    private void createQuestion(Block block, String text, String opt1, String opt2, 
                                String opt3, String opt4, int correct) {
        Question question = new Question();
        question.setBlock(block);
        question.setText(text);
        question.setOption1(opt1);
        question.setOption2(opt2);
        question.setOption3(opt3);
        question.setOption4(opt4);
        question.setCorrectOption(correct);
        questionRepository.save(question);
    }
}