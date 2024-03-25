#ifndef DTO_H_
#define DTO_H_

#include <vector>
#include <string>

namespace ekyc_card_detection {

    struct Output {
        struct Card {
            std::vector<std::vector<float> > coords;
            float confidence;
            std::string class_name;
        };
        std::vector<Card> cards;
    };

    enum class LoadState {
        kLoadSuccess,
        kLoadParamFailed,
        kLoadBinFailed,
        kNumState,
    };

}

#endif // DTO_H_